package models;

import akka.actor.Cancellable;
import akka.stream.javadsl.Source;
import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Screen;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class ScheduleTicker extends Observable implements Runnable {

	private Queue<Flux> fluxes;
	private List<Screen> screens;
	private List<String> macs;
	private Flux currentFlux;

	private boolean isRunning;

	public ScheduleTicker() {
		fluxes = new LinkedList<>();
		screens = new ArrayList<>();
		macs = new ArrayList<>();

		isRunning = false;
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

	private void activate() {
		isRunning = true;
	}

	@Override
	public void run() {

		while (isRunning = true) {
			// to make the flux rotation
			fluxes.add(fluxes.element());
			currentFlux = fluxes.remove();

			final Source<String, Cancellable> tickSource =
				Source.tick(
					Duration.ZERO,
					Duration.of(currentFlux.getDuration(), ChronoUnit.MILLIS),
					"TICK");
			setChanged();
			notifyObservers(tickSource.map((tick) -> currentFlux.getUrl() + "|" + String.join(",", macs)));
		}
	}
}
