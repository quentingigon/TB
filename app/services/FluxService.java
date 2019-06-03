package services;

import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;
import models.db.ScheduledFlux;
import models.entities.FluxData;
import models.repositories.interfaces.FluxRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the service used to make operations on the database for Fluxes.
 */
public class FluxService {

	private final FluxRepository fluxRepository;

	public FluxService(FluxRepository fluxRepository) {
		this.fluxRepository = fluxRepository;
	}

	public Flux getFluxById(Integer id) {
		return fluxRepository.getById(id);
	}

	public Flux getFluxByName(String name) {
		return fluxRepository.getByName(name);
	}

	public Flux create(Flux flux) {
		return fluxRepository.addFlux(flux);
	}

	public void delete(Flux flux) {
		fluxRepository.delete(flux);
	}

	public LocatedFlux createLocated(LocatedFlux flux) {
		return fluxRepository.addLocatedFlux(flux);
	}

	public LocatedFlux getLocatedFluxByFluxId(Integer id) {
		return fluxRepository.getLocatedFluxByFluxId(id);
	}

	public GeneralFlux createGeneral(GeneralFlux flux) {
		return fluxRepository.addGeneralFlux(flux);
	}

	public GeneralFlux getGeneralFluxByFluxId(Integer id) {
		return fluxRepository.getGeneralFluxByFluxId(id);
	}

	public ScheduledFlux createScheduled(ScheduledFlux flux) {
		return fluxRepository.addScheduledFlux(flux);
	}

	public Flux update(Flux flux) {
		return fluxRepository.update(flux);
	}

	public List<Integer> getFallBackIdsOfScheduleById(Integer id) {
		return fluxRepository.getAllFallbackIdsOfSchedule(id);
	}

	public List<FluxData> getScheduledFluxesOfScheduleById(int scheduleId) {
		List<FluxData> data = new ArrayList<>();
		for (ScheduledFlux sf: fluxRepository.getAllScheduledFluxByScheduleId(scheduleId)) {
			if (fluxRepository.getById(sf.getFluxId()) != null) {
				FluxData fluxData = new FluxData(fluxRepository.getById(sf.getFluxId()));
				fluxData.setStartTime(BlockUtils.getTimeOfBlockNumber(sf.getStartBlock()));
				data.add(fluxData);
			}
		}
		return data;
	}

	public List<FluxData> getUnscheduledFluxesOfScheduleById(int scheduleId) {
		List<FluxData> data = new ArrayList<>();
		for (Flux f: fluxRepository.getAllUnscheduledFluxByScheduleId(scheduleId)) {
			if (fluxRepository.getById(f.getId()) != null) {
				FluxData fluxData = new FluxData(fluxRepository.getById(f.getId()));
				data.add(fluxData);
			}
		}
		return data;
	}



	public List<FluxData> getAllFluxesOfScheduleById(int scheduleId) {
		List<FluxData> output = getScheduledFluxesOfScheduleById(scheduleId);
		output.addAll(getUnscheduledFluxesOfScheduleById(scheduleId));
		return output;
	}

	public List<FluxData> getAllFluxes() {
		List<FluxData> data = new ArrayList<>();
		for (Flux f: fluxRepository.getAll()) {
			data.add(new FluxData(f));
		}
		return data;
	}

	public List<FluxData> getAllFluxesOfTeam(int teamId) {
		List<FluxData> data = new ArrayList<>();
		for (Integer fluxId : fluxRepository.getAllFluxIdsOfTeam(teamId)) {
			if (fluxRepository.getById(fluxId) != null)
				data.add(new FluxData(fluxRepository.getById(fluxId)));
		}
		return data;
	}

	public List<FluxData> getAllLocatedFluxesOfTeam(int teamId) {
		List<FluxData> data = new ArrayList<>();
		for (Integer fluxId : fluxRepository.getAllFluxIdsOfTeam(teamId)) {
			if (fluxRepository.getLocatedFluxByFluxId(fluxId) != null)
				data.add(new FluxData(fluxRepository.getById(fluxId)));
		}
		return data;
	}

	public List<FluxData> getAllGeneralFluxesOfTeam(int teamId) {
		List<FluxData> data = new ArrayList<>();
		for (Integer fluxId : fluxRepository.getAllFluxIdsOfTeam(teamId)) {
			if (fluxRepository.getGeneralFluxByFluxId(fluxId) != null)
				data.add(new FluxData(fluxRepository.getById(fluxId)));
		}
		return data;
	}
}
