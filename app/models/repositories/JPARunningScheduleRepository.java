package models.repositories;

import models.db.RunningSchedule;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class JPARunningScheduleRepository implements RunningScheduleRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPARunningScheduleRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public RunningSchedule add(RunningSchedule schedule) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(schedule);
		});
		return schedule;
	}

	@Override
	public RunningSchedule getByName(String name) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleName = "'" + name + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM runningschedules WHERE name = " + scheduleName, RunningSchedule.class);
			try {
				return (RunningSchedule) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<RunningSchedule> getAll() {
		return jpaApi.withTransaction(entityManager -> {
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM runningschedules", RunningSchedule.class);
			try {
				return (List<RunningSchedule>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public void update(RunningSchedule schedule) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(schedule);
		});
	}

	@Override
	public void delete(RunningSchedule schedule) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(schedule) ? schedule : entityManager.merge(schedule));
		});
	}
}
