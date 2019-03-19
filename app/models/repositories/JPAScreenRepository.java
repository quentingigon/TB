package models.repositories;

import models.Screen;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
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
			Query query = entityManager.createNativeQuery("select from screens where mac == " + address);
			return (Screen) query.getSingleResult();
		});
	}

	@Override
	public void add(Screen screen) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(screen);
		});
	}
}
