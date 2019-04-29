package models.db;

import models.entities.TeamData;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="teams", schema="public")
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;

	@Column(name="name")
	private String name;

	@OneToMany
	@Column(name="teams_screens")
	private List<Screen> screens;

	@OneToMany
	@Column(name="screengroups")
	private List<ScreenGroup> screenGroups;

	@OneToMany
	@Column(name="schedules")
	private List<Schedule> schedules;

	@OneToMany
	@Column(name="diffusers")
	private List<Diffuser> diffusers;

	@OneToMany
	@Column(name="fluxes")
	private List<Flux> fluxes;

	public Team() {
	}

	public Team(String name) {
		this.name = name;

		this.fluxes = new ArrayList<>();
		this.diffusers = new ArrayList<>();
		this.schedules = new ArrayList<>();
		this.screenGroups = new ArrayList<>();
	}

	public Team(TeamData data) {
		name = data.getName();


	}

	private void fillFluxes(List<String> fluxesNames) {
		fluxes = new ArrayList<>();
		for (String name: fluxesNames) {

		}
	}

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ScreenGroup> getScreenGroups() {
		return screenGroups;
	}

	public void setScreenGroups(List<ScreenGroup> screenGroups) {
		this.screenGroups = screenGroups;
	}

	public void addToScreenGroups(ScreenGroup group) {
		this.screenGroups.add(group);
	}

	public List<Schedule> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Schedule> schedules) {
		this.schedules = schedules;
	}

	public void addToSchedules(Schedule schedule) {
		this.schedules.add(schedule);
	}

	public List<Diffuser> getDiffusers() {
		return diffusers;
	}

	public void setDiffusers(List<Diffuser> diffusers) {
		this.diffusers = diffusers;
	}

	public void addToDiffusers(Diffuser diffuser) {
		this.diffusers.add(diffuser);
	}

	public List<Flux> getFluxes() {
		return fluxes;
	}

	public void setFluxes(List<Flux> fluxes) {
		this.fluxes = fluxes;
	}

	public void addToFluxes(Flux flux) {
		this.fluxes.add(flux);
	}

	public List<Screen> getScreens() {
		return screens;
	}

	public void setScreens(List<Screen> screens) {
		this.screens = screens;
	}

	public void addScreen(Screen s) {
		if (screens == null)
			screens = new ArrayList<>();
		screens.add(s);
	}
}
