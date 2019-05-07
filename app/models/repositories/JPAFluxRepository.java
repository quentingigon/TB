package models.repositories;

import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;
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
	public Flux addFlux(Flux flux) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(flux);
		});
		return flux;
	}

	@Override
	public LocatedFlux addLocatedFlux(LocatedFlux flux) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(flux);
		});
		return flux;
	}

	@Override
	public GeneralFlux addGeneralFlux(GeneralFlux flux) {
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
				"SELECT * FROM flux WHERE name = " + fluxName, Flux.class);
			try {
				return (Flux) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public Flux getById(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String fluxId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM flux WHERE flux_id = " + fluxId, Flux.class);
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
				"SELECT * FROM flux", Flux.class);
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
