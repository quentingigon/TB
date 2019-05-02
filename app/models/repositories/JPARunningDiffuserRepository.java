package models.repositories;

import models.db.RunningDiffuser;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class JPARunningDiffuserRepository implements RunningDiffuserRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPARunningDiffuserRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public RunningDiffuser add(RunningDiffuser diffuser) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(diffuser);
		});
		return diffuser;
	}

	@Override
	public RunningDiffuser getByName(String name) {
		return jpaApi.withTransaction(entityManager -> {
			String diffuserName = "'" + name + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM runningdiffusers WHERE name = " + diffuserName, RunningDiffuser.class);
			try {
				return (RunningDiffuser) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<RunningDiffuser> getAll() {
		return jpaApi.withTransaction(entityManager -> {
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM runningdiffusers", RunningDiffuser.class);
			try {
				return (List<RunningDiffuser>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public void update(RunningDiffuser diffuser) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(diffuser);
		});
	}

	@Override
	public void delete(RunningDiffuser diffuser) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(diffuser) ? diffuser : entityManager.merge(diffuser));
		});
	}
}
