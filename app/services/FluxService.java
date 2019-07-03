package services;

import models.db.*;
import models.entities.FluxData;
import models.repositories.interfaces.FluxRepository;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static controllers.CronUtils.mustFluxLoopBeStarted;

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

	public FluxTrigger createFluxTrigger(FluxTrigger ft) {
		return fluxRepository.addFluxTrigger(ft);
	}

	public FluxLoop createFluxLoop(FluxLoop fl) {
		return fluxRepository.addFluxLoop(fl);
	}

	public void addFluxToFluxLoop(Integer loopId, Integer fluxId, Integer order) {
		fluxRepository.addFluxToFluxLoopWithOrder(loopId, fluxId, order);
	}

	public List<FluxTrigger> getFluxTriggersOfScheduleById(Integer id) {
		return fluxRepository.getAllFluxTriggerOfSchedule(id);
	}

	public List<FluxLoop> getFluxLoopOfScheduleById(Integer id) {
		return fluxRepository.getAllFluxLoopOfSchedule(id);
	}

	public FluxLoop update(FluxLoop loop) {
		return fluxRepository.update(loop);
	}

	public Flux update(Flux flux) {
		return fluxRepository.update(flux);
	}

	public List<Integer> getFallBackIdsOfScheduleById(Integer id) {
		return fluxRepository.getAllFallbackIdsOfSchedule(id);
	}

	public List<Integer> getUnscheduledIdsOfSchedule(Integer id) {
		return fluxRepository.getAllUnscheduledIdsOfSchedule(id);
	}

	public List<FluxData> getScheduledFluxesOfScheduleById(int scheduleId) {
		List<FluxData> data = new ArrayList<>();
		for (ScheduledFlux sf: fluxRepository.getAllScheduledFluxByScheduleId(scheduleId)) {
			if (fluxRepository.getById(sf.getFluxId()) != null) {
				FluxData fluxData = new FluxData(fluxRepository.getById(sf.getFluxId()));
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

	public FluxLoop getFluxLoopThatMustBeStarted(List<FluxTrigger> triggers, Schedule schedule) {

		String currentTime = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());

		List<FluxLoop> loopFluxes = getFluxLoopOfScheduleById(schedule.getId());

		for (FluxLoop loop: loopFluxes) {
			// if there is a FluxLoop fot the current time or
			// if current time is between a FluxLoop and a FluxTrigger or
			// if there are no FluxTrigger from the current time
			if (mustFluxLoopBeStarted(currentTime, loop, triggers)) {
				DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
				LocalTime timeOfExecution = formatter.parseLocalTime(currentTime)
					.plusMinutes(1);
				loop.setStartTime(formatter.print(timeOfExecution));
				// update(loop);
				return loop;
			}
		}
		return null;
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
