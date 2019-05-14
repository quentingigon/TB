package models.entities;

import models.db.Team;

import java.util.List;

public class TeamData {

	private String name;
	private List<String> admins;
	private List<String> members;
	private List<String> fluxes;
	private List<String> screens;
	private List<String> schedules;
	private List<String> diffusers;
	private List<String> groups;

	public TeamData() {
	}

	public TeamData(String name) {
		this.name = name;
	}

	public TeamData(Team team) {
		this.name = team.getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getMembers() {
		return members;
	}

	public void setMembers(List<String> members) {
		this.members = members;
	}

	public List<String> getFluxes() {
		return fluxes;
	}

	public void setFluxes(List<String> fluxes) {
		this.fluxes = fluxes;
	}

	public List<String> getScreens() {
		return screens;
	}

	public void setScreens(List<String> screens) {
		this.screens = screens;
	}

	public List<String> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<String> schedules) {
		this.schedules = schedules;
	}

	public List<String> getDiffusers() {
		return diffusers;
	}

	public void setDiffusers(List<String> diffusers) {
		this.diffusers = diffusers;
	}

	public List<String> getGroups() {
		return groups;
	}

	public void setGroups(List<String> groups) {
		this.groups = groups;
	}

	public List<String> getAdmins() {
		return admins;
	}

	public void setAdmins(List<String> admins) {
		this.admins = admins;
	}
}
