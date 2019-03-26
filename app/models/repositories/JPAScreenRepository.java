package models.repositories;

import models.Screen;
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
	public void add(Screen screen) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(screen);
		});
	}
}
