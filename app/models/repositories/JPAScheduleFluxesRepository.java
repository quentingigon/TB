package models.repositories;

import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class JPAScheduleFluxesRepository implements ScheduleFluxesRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPAScheduleFluxesRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}


	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getFluxesIdsByScheduleId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT DISTINCT fluxes FROM schedule_fluxes WHERE schedule_schedule_id = " + scheduleId);
			try {
				return (List<Integer>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}
}
