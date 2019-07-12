package models.repositories;

import models.db.*;
import models.repositories.interfaces.FluxRepository;
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
	public FluxTrigger addFluxTrigger(FluxTrigger flux) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(flux);
		});
		return flux;
	}

	@Override
	public FluxLoop addFluxLoop(FluxLoop flux) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.persist(flux);
		});
		return flux;
	}

	@Override
	public void addFluxToFluxLoopWithOrder(Integer loopId, Integer fluxId, Integer order) {
		jpaApi.withTransaction(entityManager -> {
			String fluxloopId = "'" + loopId + "'";
			String fluxes = "'" + fluxId + "'";
			String fluxOrder = "'" + order + "'";

			entityManager.createNativeQuery("INSERT INTO fluxloop_fluxes(fluxloop_id, fluxes, flux_order) VALUES (?,?,?)")
      			.setParameter(1, loopId)
      			.setParameter(2, fluxId)
      			.setParameter(3, order)
      			.executeUpdate();
		});
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
	public LocatedFlux getLocatedFluxByFluxId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			Flux flux = getById(id);
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM locatedflux WHERE flux_id = " + flux.getId(),
				LocatedFlux.class);
			try {
				return (LocatedFlux) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public GeneralFlux getGeneralFluxByFluxId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			Flux flux = getById(id);
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM generalflux WHERE flux_id = " + flux.getId(),
				GeneralFlux.class);
			try {
				return (GeneralFlux) query.getSingleResult();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<FluxLoop> getAllFluxLoopOfSchedule(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM fluxloop WHERE schedule_id = " + scheduleId,
				FluxLoop.class);
			try {
				return (List<FluxLoop>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<ScheduledFlux> getAllScheduledFluxByScheduleId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM scheduled_flux WHERE schedule_id = " + scheduleId,
				ScheduledFlux.class);
			try {
				return (List<ScheduledFlux>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Flux> getAllUnscheduledFluxByScheduleId(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM schedule_fluxes WHERE schedule_schedule_id = " + scheduleId,
				Flux.class);
			try {
				return (List<Flux>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<LoopedFlux> getAllLoopedFluxesOfFluxLoop(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String fluxLoopId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM fluxloop_fluxes WHERE fluxloop_id = " + fluxLoopId,
				LoopedFlux.class);
			try {
				return (List<LoopedFlux>) query.getResultList();
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
	@SuppressWarnings("unchecked")
	public List<Integer> getAllFluxIdsOfTeam(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String teamId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT DISTINCT fluxes FROM team_fluxes WHERE team_team_id = " + teamId);
			try {
				return (List<Integer>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getAllFallbackIdsOfSchedule(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT DISTINCT fallbacks FROM schedule_fallbacks WHERE schedule_schedule_id = " + scheduleId);
			try {
				return (List<Integer>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Integer> getAllUnscheduledIdsOfSchedule(Integer id) {
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

	@Override
	@SuppressWarnings("unchecked")
	public List<FluxTrigger> getAllFluxTriggerOfSchedule(Integer id) {
		return jpaApi.withTransaction(entityManager -> {
			String scheduleId = "'" + id + "'";
			Query query = entityManager.createNativeQuery(
				"SELECT * FROM fluxtrigger WHERE schedule_id = " + scheduleId,
				FluxTrigger.class);
			try {
				return (List<FluxTrigger>) query.getResultList();
			} catch (NoResultException e) {
				return null;
			}
		});
	}

	@Override
	public FluxLoop update(FluxLoop loop) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(loop);
		});
		return loop;
	}

	@Override
	public Flux update(Flux flux) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.merge(flux);
		});
		return flux;
	}

	@Override
	public void delete(Flux flux) {
		jpaApi.withTransaction(entityManager -> {
			entityManager.remove(entityManager.contains(flux) ? flux : entityManager.merge(flux));
		});
	}
}
