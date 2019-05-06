package models.repositories;

import play.db.jpa.JPAApi;

import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class JPARunningScheduleScreensRepository implements RunningScheduleScreensRepository {

	private final JPAApi jpaApi;

	@Inject
	public JPARunningScheduleScreensRepository(JPAApi jpaApi) {
		this.jpaApi = jpaApi;
	}


	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getScreensIdsByRunningScheduleId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String runningScheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT DISTINCT screens FROM runningdiffuser_screens WHERE runningdiffuser_runningdiffuser_id = " + runningScheduleId);
			try {
				return (List<Integer>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}
}
