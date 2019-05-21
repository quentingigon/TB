package models.repositories;

import models.db.Team;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class JPATeamRepository implements TeamRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPATeamRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public Team add(Team team) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(team);
		});
		return team;
	}

	@Override
	public Team getByName(String name) {
		return jpaApi.withTransaction(entityManager -> {
			String teamName = "'" + name + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM team WHERE name = " + teamName, Team.class);
			try {
				return (Team) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public Team getById(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String teamId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM team WHERE team_id = " + teamId, Team.class);
			try {
				return (Team) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Team> getAll() {
		return jpaApi.withTransaction(entityManager -> {

			Query query = entityManager.createNativeQuery(
				"SELECT * FROM team", Team.class);
			try {
				return (List<Team>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public Team update(Team team) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(team);
		});
		return team;
	}

	@Override
	public void delete(Team team) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(team) ? team : entityManager.merge(team));
		});
	}
}
