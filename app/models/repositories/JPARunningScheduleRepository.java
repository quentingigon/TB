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
	public RunningSchedule getById(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String runningScheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM runningschedule WHERE runningschedule_id = " + runningScheduleId,
				RunningSchedule.class);
			try {
				return (RunningSchedule) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public RunningSchedule getByScheduleId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM runningschedule WHERE schedule_id = " + scheduleId,
				RunningSchedule.class);
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
				"SELECT * FROM runningschedule", RunningSchedule.class);
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
