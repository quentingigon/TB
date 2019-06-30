package models.repositories;

import models.db.ScheduledFlux;
import models.repositories.interfaces.ScheduleFluxesRepository;
import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;

public class JPAScheduleFluxesRepository implements ScheduleFluxesRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPAScheduleFluxesRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}


	@Override
	public ScheduledFlux getById(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduledFluxId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM scheduled_flux WHERE scheduled_flux_id = " + scheduledFluxId,
				ScheduledFlux.class);
			try {
				return (ScheduledFlux) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}
}
