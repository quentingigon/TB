package models.entities;

import models.db.Schedule;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the Schedule data being sent between clients and server.
 */
public class ScheduleData {

	private String name;
	private List<String> fluxes;
	private List<String> unscheduledFluxes;
	private List<String> fallbackFluxes;
	private List<String> screens;
	private List<String> days;

	private boolean activated;

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

	// Getters and setters

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

	public List<String> getUnscheduledFluxes() {
		return unscheduledFluxes;
	}

	public void setUnscheduledFluxes(List<String> unscheduledFluxes) {
		this.unscheduledFluxes = unscheduledFluxes;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public List<String> getDays() {
		return days;
	}

	public void setDays(List<String> days) {
		this.days = days;
	}
}
