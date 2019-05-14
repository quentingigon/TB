package models.entities;

import models.db.Schedule;

import java.util.ArrayList;
import java.util.List;

public class ScheduleData {

	private String name;
	private List<String> fluxes;
	private List<String> scheduledFluxes;
	private List<String> fallbackFluxes;
	private List<String> screens;

	public ScheduleData() {
	}

	public ScheduleData(String name) {
		this.name = name;
	}

	public ScheduleData(Schedule schedule) {
		name = schedule.getName();
		fluxes = new ArrayList<>();
		fallbackFluxes = new ArrayList<>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getFluxes() {
		return fluxes;
	}

	public void setFluxes(List<String> fluxes) {
		this.fluxes = fluxes;
	}

	public List<String> getFallbackFluxes() {
		return fallbackFluxes;
	}

	public void setFallbackFluxes(List<String> fallbackFluxes) {
		this.fallbackFluxes = fallbackFluxes;
	}

	public List<String> getScreens() {
		return screens;
	}

	public void setScreens(List<String> screens) {
		this.screens = screens;
	}

	public List<String> getScheduledFluxes() {
		return scheduledFluxes;
	}

	public void setScheduledFluxes(List<String> scheduledFluxes) {
		this.scheduledFluxes = scheduledFluxes;
	}
}
