package models.repositories;

import models.User;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.EntityManager;

@Singleton
public class JPAUserRepository implements UserRepository{

	private final JPAApi jpaApi;

	@Inject
	public JPAUserRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public void create(User user) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(user);
		});
	}

	private User get(EntityManager em, String email) {
		return em.createQuery("select from User where email = " + email, User.class).getSingleResult();
	}
}
