package models.db;

import java.util.ArrayList;
import java.util.List;

public class RunningSchedule extends Schedule {

	private List<Screen> screens;
	private Flux currentFlux;

	public RunningSchedule() {
		super();
	}

	public RunningSchedule(Schedule schedule) {
		this.name = schedule.getName();
		this.fluxes = schedule.getFluxes();
		this.screens = new ArrayList<>();
	}

	public List<Screen> getScreens() {
		return screens;
	}

	public void setScreens(List<Screen> screens) {
		this.screens = screens;
	}

	public Flux getCurrentFlux() {
		return currentFlux;
	}

	public void setCurrentFlux(Flux currentFlux) {
		this.currentFlux = currentFlux;
	}
}
