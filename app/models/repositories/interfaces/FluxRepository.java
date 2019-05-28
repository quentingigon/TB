package models.repositories.interfaces;

import com.google.inject.ImplementedBy;
import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;
import models.db.ScheduledFlux;
import models.repositories.JPAFluxRepository;

import java.util.List;

@ImplementedBy(JPAFluxRepository.class)
public interface FluxRepository {

	Flux addFlux(Flux flux);
	LocatedFlux addLocatedFlux(LocatedFlux flux);
	GeneralFlux addGeneralFlux(GeneralFlux flux);
	ScheduledFlux addScheduledFlux(ScheduledFlux flux);

	Flux getByName(String name);
	Flux getById(Integer id);
	LocatedFlux getLocatedFluxByFluxId(Integer id);
	GeneralFlux getGeneralFluxByFluxId(Integer id);

	List<ScheduledFlux> getAllScheduledFluxByScheduleId(Integer id);
	List<Flux> getAllUnscheduledFluxByScheduleId(Integer id);

	List<Flux> getAll();
	List<Integer> getAllFluxIdsOfTeam(Integer id);

	Flux update(Flux flux);
	void delete(Flux flux);
}
