package models.repositories;

import models.db.Screen;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class JPAScreenRepository implements ScreenRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPAScreenRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public Screen getByMacAddress(String address) {
		return jpaApi.withTransaction(entityManager -> {
			String macAdr = "'" + address + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM screen WHERE mac_address = " + macAdr, Screen.class);
			try {
				return (Screen) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}

		});
	}

	@Override
	public Screen getById(int id) {
		return jpaApi.withTransaction(entityManager -> {
			String ID = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM screen WHERE screen_id = " + ID, Screen.class);
			try {
				return (Screen) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}

		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Screen> getAll() {
		return jpaApi.withTransaction(entityManager -> {

			Query query = entityManager.createNativeQuery(
				"SELECT * FROM screen", Screen.class);
			try {
				return (List<Screen>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getAllScreenIdsOfTeam(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String teamId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT DISTINCT screens FROM team_screens WHERE team_team_id = " + teamId);
			try {
				return (List<Integer>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Screen> getAllByRunningScheduleId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String runningScheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM screen WHERE runningschedule_id = " + runningScheduleId,
				Screen.class);
			try {
				return (List<Screen>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public void add(Screen screen) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(screen);
		});
	}

	@Override
	public void update(Screen screen) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(screen);
		});
	}

	@Override
	public void delete(Screen screen) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(screen) ? screen : entityManager.merge(screen));
		});
	}
}
