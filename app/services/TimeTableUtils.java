package services;

import models.db.Flux;
import models.db.Schedule;
import models.db.ScheduledFlux;
import models.repositories.interfaces.*;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;

import static services.BlockUtils.blockNumber;

public class TimeTableUtils {

	@Inject
	FluxRepository fluxRepository;

	@Inject
	public TimeTableUtils() {
	}

	public HashMap<Integer, Integer> getTimeTable(Schedule schedule) {

		List<ScheduledFlux> scheduledFluxes = fluxRepository.getAllScheduledFluxByScheduleId(schedule.getId());
		Flux lastFlux = new Flux();
		long lastFluxDuration = 0;
		boolean noFluxSent;

		HashMap<Integer, Integer> timetable = new HashMap<>();
		for (int i = 0; i < blockNumber; i++) {

			noFluxSent = true;

			// if duration of last inserted ScheduledFlux is still not finished iterating over
			// we put last flux in the schedule
			if (lastFluxDuration != 0) {
				lastFluxDuration--;
				timetable.put(i, lastFlux.getId());
			}
			else {
				// check if we must insert fluxes at a certain hour
				for (ScheduledFlux sf : scheduledFluxes) {
					// a flux is set to begin at this block
					if (sf.getStartBlock().equals(i)) {
						Flux flux = fluxRepository.getById(sf.getFluxId());
						lastFlux = flux;
						lastFluxDuration = flux.getTotalDuration() - 1;
						timetable.put(i, flux.getId());
						noFluxSent = false;
						scheduledFluxes.remove(sf);
						break;
					}
				}

				if (noFluxSent) {
					// if no flux is set at this block
					timetable.put(i, -1);
				}
			}
		}
		return timetable;
	}
}
