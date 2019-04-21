package models.db;

import java.util.List;

public class RunningSchedule extends Schedule {

	private List<Screen> screens;
	private Flux currentFlux;

	public RunningSchedule() {
		currentFlux = getFluxes().get(0);
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
