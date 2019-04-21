package models;

import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Screen;

import java.util.*;

public class ScheduleTicker extends Observable implements Runnable {

	private Queue<Flux> fluxes;
	private List<Flux> currentFluxes;
	private List<Screen> screens;
	private List<String> macs;
	private List<RunningSchedule> runningSchedules;
	private Flux currentFlux;

	private boolean isRunning;

	public ScheduleTicker() {
		fluxes = new LinkedList<>();
		screens = new ArrayList<>();
		macs = new ArrayList<>();
		runningSchedules = new ArrayList<>();

		isRunning = false;
	}

	public ScheduleTicker(List<RunningSchedule> runningSchedules) {
		this.runningSchedules = runningSchedules;
	}

	public ScheduleTicker(Queue<Flux> fluxes, List<Screen> screens) {
		this.fluxes = fluxes;
		this.screens = screens;
		getMacAddresses();

		isRunning = false;
	}

	public ScheduleTicker(RunningSchedule schedule) {
		this.fluxes = new LinkedList<>(schedule.getFluxes());
		this.screens = schedule.getScreens();
		getMacAddresses();

		isRunning = false;
	}

	private void getMacAddresses() {
		macs = new ArrayList<>();

		for(Screen s: screens) {
			macs.add(s.getMacAddress());
		}
	}

	public void activate() {
		isRunning = true;
	}

	public void deactivate() { isRunning = false;}

	@Override
	public void run() {


		/*
		while (!runningSchedules.isEmpty()) {

			currentFluxes = new ArrayList<>();

			for (RunningSchedule rs: runningSchedules) {
				currentFluxes.add(rs.getCurrentFlux());
			}

			currentFluxes.sort((o1, o2) -> Integer.compare(o2.getDuration(), o1.getDuration()));

			Queue<Flux> fluxesQueue = (LinkedList<Flux>) currentFluxes;

			while (!fluxesQueue.isEmpty()) {

				currentFlux = fluxesQueue.remove();

				final Source<String, Cancellable> tickSource =
					Source.tick(
						Duration.ZERO,
						Duration.of(currentFlux.getDuration(), ChronoUnit.MILLIS),
						"TICK");
				setChanged();
				notifyObservers(tickSource.map((tick) -> currentFlux.getUrl() + "|" + String.join(",", macs)));
			}

			try {
				Thread.sleep(currentFlux.getDuration());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}*/
	}
}
