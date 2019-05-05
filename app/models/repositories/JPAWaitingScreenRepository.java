package models.repositories;

import models.db.WaitingScreen;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class JPAWaitingScreenRepository implements WaitingScreenRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPAWaitingScreenRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public WaitingScreen add(WaitingScreen waitingScreen) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(waitingScreen);
		});
		return waitingScreen;
	}

	@Override
	public void delete(WaitingScreen waitingScreen) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(waitingScreen);
		});
	}

	@Override
	public WaitingScreen getByMac(String mac) {
		return jpaApi.withTransaction(entityManager -> {
			String macAdr = "'" + mac + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM waitingscreen WHERE mac_address = " + macAdr, WaitingScreen.class);
			try {
				return (WaitingScreen) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}
}
