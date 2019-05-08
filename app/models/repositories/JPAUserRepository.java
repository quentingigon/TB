package models.repositories;

import models.db.TeamMember;
import models.db.User;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

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
	public User get(String email, String password) {
		return jpaApi.withTransaction(entityManager -> {
			String mail = "'" + email + "'";
			String pw = "'" + password + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM users WHERE email = " + mail
					+ " and password = " + pw, User.class);
			try {
				return (User) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public void delete(User user) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
		});
	}

	@Override
	public void update(User user) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(user);
		});
	}

	@Override
	public User getByEmail(String email) {
		return jpaApi.withTransaction(entityManager -> {
			String mail = "'" + email + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM users WHERE email = " + mail,
				User.class);
			try {
				return (User) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public User getById(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String userId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM users WHERE user_id = " + userId,
				User.class);
			try {
				return (User) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<User> getAll() {
		return jpaApi.withTransaction(entityManager -> {

			Query query = entityManager.createNativeQuery(
				"SELECT * FROM users", User.class);
			try {
				return (List<User>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getAllMemberIdsOfTeam(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String teamId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT DISTINCT members FROM team_members WHERE team_team_id = " + teamId);
			try {
				return (List<Integer>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public void createMember(TeamMember member) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(member);
		});
	}
}
