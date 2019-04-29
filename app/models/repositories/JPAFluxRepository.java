package models.repositories;

import models.db.Flux;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class JPAFluxRepository implements FluxRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPAFluxRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}

	@Override
	public Flux add(Flux flux) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(flux);
		});
		return flux;
	}

	@Override
	public Flux getByName(String name) {
		return jpaApi.withTransaction(entityManager -> {
			String fluxName = "'" + name + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM fluxes WHERE name = " + fluxName, Flux.class);
			try {
				return (Flux) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public Flux getByUrl(String url) {
		return jpaApi.withTransaction(entityManager -> {
			String fluxUrl = "'" + url + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM fluxes WHERE url = " + fluxUrl, Flux.class);
			try {
				return (Flux) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Flux> getAll() {
		return jpaApi.withTransaction(entityManager -> {
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM fluxes", Flux.class);
			try {
				return (List<Flux>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public void update(Flux flux) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(flux);
		});
	}

	@Override
	public void delete(Flux flux) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(flux) ? flux : entityManager.merge(flux));
		});
	}
}
