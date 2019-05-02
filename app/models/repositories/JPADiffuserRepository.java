package models.repositories;

import models.db.Diffuser;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class JPADiffuserRepository implements DiffuserRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPADiffuserRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public Diffuser add(Diffuser diffuser) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(diffuser);
		});
		return diffuser;
	}

	@Override
	public void update(Diffuser diffuser) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(diffuser);
		});
	}

	@Override
	public void delete(Diffuser diffuser) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(diffuser) ? diffuser : entityManager.merge(diffuser));
		});
	}

	@Override
	public Diffuser getByName(String name) {
		return jpaApi.withTransaction(entityManager -> {
			String diffuserName = "'" + name + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM diffusers WHERE name = " + diffuserName, Diffuser.class);
			try {
				return (Diffuser) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Diffuser> getAll() {
		return jpaApi.withTransaction(entityManager -> {
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM diffusers", Diffuser.class);
			try {
				return (List<Diffuser>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}
}
