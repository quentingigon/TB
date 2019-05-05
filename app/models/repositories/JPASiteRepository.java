package models.repositories;

import models.db.Site;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class JPASiteRepository implements SiteRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPASiteRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public Site getByName(String name) {
		return jpaApi.withTransaction(entityManager -> {
			String siteName = "'" + name + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM site WHERE name = " + siteName, Site.class);
			try {
				return (Site) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}

		});
	}
}
