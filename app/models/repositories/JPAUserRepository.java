package models.repositories;

import models.User;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.Query;

@Singleton
public class JPAUserRepository implements UserRepository{

	private final JPAApi jpaApi;

	@Inject
	public JPAUserRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public User create(User user) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(user);
		});
		return user;
	}

	@Override
	public User getByEmail(String email) {
		return jpaApi.withTransaction(entityManager -> {
			String mail = "'" + email + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM users WHERE email = " + mail, User.class);
			try {
				return (User) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}
}
