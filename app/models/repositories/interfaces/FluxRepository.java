package models.repositories.interfaces;

import com.google.inject.ImplementedBy;
import models.db.*;
import models.repositories.JPAFluxRepository;

import java.util.List;

/**
 * This interface defines the functions for Flux database operations.
 */
@ImplementedBy(JPAFluxRepository.class)
public interface FluxRepository {

	Flux addFlux(Flux flux);
	LocatedFlux addLocatedFlux(LocatedFlux flux);
	GeneralFlux addGeneralFlux(GeneralFlux flux);
	ScheduledFlux addScheduledFlux(ScheduledFlux flux);
	FluxTrigger addFluxTrigger(FluxTrigger flux);
	FluxLoop addFluxLoop(FluxLoop flux);

	Flux getByName(String name);
	Flux getById(Integer id);
	LocatedFlux getLocatedFluxByFluxId(Integer id);
	GeneralFlux getGeneralFluxByFluxId(Integer id);

	List<FluxLoop> getAllFluxLoopOfSchedule(Integer id);
	List<ScheduledFlux> getAllScheduledFluxByScheduleId(Integer id);
	List<Flux> getAllUnscheduledFluxByScheduleId(Integer id);

	List<Flux> getAll();
	List<Integer> getAllFluxIdsOfTeam(Integer id);
	List<Integer> getAllFallbackIdsOfSchedule(Integer id);
	List<Integer> getAllUnscheduledIdsOfSchedule(Integer id);
	List<FluxTrigger> getAllFluxTriggerOfSchedule(Integer id);

	FluxLoop update(FluxLoop loop);
	Flux update(Flux flux);
	void delete(Flux flux);
}
