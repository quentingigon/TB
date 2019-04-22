package models.repositories;

import models.db.Screen;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;

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
				"SELECT * FROM screens WHERE mac = " + macAdr, Screen.class);
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
				"SELECT * FROM screens WHERE id = " + ID, Screen.class);
			try {
				return (Screen) query.getSingleResult();
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
			entityManager.remove(screen);
		});
	}
}
