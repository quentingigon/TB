package models.db;

import models.entities.TeamData;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="team", schema="public")
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="team_id")
	private Integer id;

	@Column(name="name")
	private String name;

	@ElementCollection
	private List<Integer> screens;

	@ElementCollection
	private List<Integer> groups;

	@ElementCollection
	private List<Integer> schedules;

	@ElementCollection
	private List<Integer> diffusers;

	@ElementCollection
	private List<Integer> fluxes;

	@ElementCollection
	private List<Integer> members;

	public Team() {
	}

	public Team(String name) {
		this.name = name;

		this.fluxes = new ArrayList<>();
		this.diffusers = new ArrayList<>();
		this.schedules = new ArrayList<>();
		this.groups = new ArrayList<>();
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

	public List<Integer> getGroups() {
		return groups;
	}

	public void setGroups(List<Integer> screenGroups) {
		this.groups = screenGroups;
	}

	public void addToScreenGroups(Integer group) {
		this.groups.add(group);
	}

	public List<Integer> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Integer> schedules) {
		this.schedules = schedules;
	}

	public void addToSchedules(Integer schedule) {
		this.schedules.add(schedule);
	}

	public List<Integer> getDiffusers() {
		return diffusers;
	}

	public void setDiffusers(List<Integer> diffusers) {
		this.diffusers = diffusers;
	}

	public void addToDiffusers(Integer diffuser) {
		this.diffusers.add(diffuser);
	}

	public List<Integer> getFluxes() {
		return fluxes;
	}

	public void setFluxes(List<Integer> fluxes) {
		this.fluxes = fluxes;
	}

	public void addToFluxes(Integer flux) {
		this.fluxes.add(flux);
	}

	public List<Integer> getScreens() {
		return screens;
	}

	public void setScreens(List<Integer> screens) {
		this.screens = screens;
	}

	public void addScreen(Integer s) {
		if (screens == null)
			screens = new ArrayList<>();
		screens.add(s);
	}

	public List<Integer> getMembers() {
		return members;
	}

	public void setMembers(List<Integer> members) {
		this.members = members;
	}
}
