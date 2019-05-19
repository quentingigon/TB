package models.repositories;

import models.db.Admin;
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
	@SuppressWarnings("unchecked")
	public List<Integer> getAllAdminIdsOfTeam(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String teamId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT DISTINCT admins FROM team_admins WHERE team_team_id = " + teamId);
			try {
				return (List<Integer>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public TeamMember createMember(TeamMember member) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(member);
		});
		return member;
	}

	@Override
	public TeamMember getMemberByUserEmail(String email) {
		return jpaApi.withTransaction(entityManager -> {

			User user = getByEmail(email);

			if (user != null) {
				Query query = entityManager.createNativeQuery(
					"SELECT * FROM teammember WHERE user_id = " + user.getId(),
					TeamMember.class);
				try {
					return (TeamMember) query.getSingleResult();
				} catch (NoResultException e) {
					return null;
				}
			}
			else
				return null;

		});
	}

	@Override
	public TeamMember getMemberByUserId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {

			User user = getById(id);

			if (user != null) {
				Query query = entityManager.createNativeQuery(
					"SELECT * FROM teammember WHERE user_id = " + user.getId(),
					TeamMember.class);
				try {
					return (TeamMember) query.getSingleResult();
				} catch (NoResultException e) {
					return null;
				}
			}
			else
				return null;

		});
	}

	@Override
	public Admin createAdmin(Admin admin) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(admin);
		});
		return admin;
	}

	@Override
	public Admin getAdminByUserEmail(String email) {
		return jpaApi.withTransaction(entityManager -> {

			User user = getByEmail(email);

			if (user != null) {
				Query query = entityManager.createNativeQuery(
					"SELECT * FROM admins WHERE user_id = " + user.getId(),
					Admin.class);
				try {
					return (Admin) query.getSingleResult();
				} catch (NoResultException e) {
					return null;
				}
			}
			else
				return null;

		});
	}

	@Override
	public Admin getAdminByUserId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {

			User user = getById(id);

			if (user != null) {
				Query query = entityManager.createNativeQuery(
					"SELECT * FROM admins WHERE user_id = " + user.getId(),
					Admin.class);
				try {
					return (Admin) query.getSingleResult();
				} catch (NoResultException e) {
					return null;
				}
			}
			else
				return null;

		});
	}
}
