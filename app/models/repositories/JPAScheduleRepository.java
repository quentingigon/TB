package models.repositories;

import models.db.RunningSchedule;
import models.db.Schedule;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class JPAScheduleRepository implements ScheduleRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPAScheduleRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public Schedule add(Schedule schedule) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(schedule);
		});
		return schedule;
	}

	@Override
	public void update(Schedule schedule) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(schedule);
		});
	}

	@Override
	public void delete(Schedule schedule) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(schedule);
		});
	}

	@Override
	public Schedule getByName(String name) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleName = "'" + name + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM schedules WHERE name = " + scheduleName, Schedule.class);
			try {
				return (Schedule) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public void activate(RunningSchedule schedule) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(schedule);
		});
	}
}
