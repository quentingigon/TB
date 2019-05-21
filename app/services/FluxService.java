package services;

import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;
import models.db.ScheduledFlux;
import models.entities.FluxData;
import models.repositories.FluxRepository;

import java.util.ArrayList;
import java.util.List;

public class FluxService {

	private final FluxRepository fluxRepository;

	public FluxService(FluxRepository fluxRepository) {
		this.fluxRepository = fluxRepository;
	}

	public Flux create(Flux flux) {
		return fluxRepository.addFlux(flux);
	}

	public LocatedFlux createLocated(LocatedFlux flux) {
		return fluxRepository.addLocatedFlux(flux);
	}

	public GeneralFlux createGeneral(GeneralFlux flux) {
		return fluxRepository.addGeneralFlux(flux);
	}

	public ScheduledFlux createScheduled(ScheduledFlux flux) {
		return fluxRepository.addScheduledFlux(flux);
	}

	public Flux update(Flux flux) {
		return fluxRepository.update(flux);
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
