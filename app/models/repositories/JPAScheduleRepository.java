package models.repositories;

import models.db.RunningSchedule;
import models.db.Schedule;
import models.db.ScheduledFlux;
import models.repositories.interfaces.ScheduleRepository;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

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
	public Schedule update(Schedule schedule) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(schedule);
		});
		return schedule;
	}

	@Override
	public void delete(Schedule schedule) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(schedule) ? schedule : entityManager.merge(schedule));
		});
	}

	@Override
	public Schedule getByName(String name) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleName = "'" + name + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM schedule WHERE name = " + scheduleName, Schedule.class);
			try {
				return (Schedule) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public Schedule getById(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM schedule WHERE schedule_id = " + scheduleId, Schedule.class);
			try {
				return (Schedule) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Schedule> getAll() {
		return jpaApi.withTransaction(entityManager -> {

			Query query = entityManager.createNativeQuery(
				"SELECT * FROM schedule", Schedule.class);
			try {
				return (List<Schedule>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getAllScheduleIdsOfTeam(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT DISTINCT schedules FROM team_schedules WHERE team_team_id = " + scheduleId);
			try {
				return (List<Integer>) query.getResultList();
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
