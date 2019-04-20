package models.repositories;

import models.db.Diffuser;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;

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
			entityManager.remove(diffuser);
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
}
