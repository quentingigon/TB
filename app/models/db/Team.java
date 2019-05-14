package models.db;

import models.entities.TeamData;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="team", schema="public")
public class Team {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="team_id")
	private Integer id;

	@Column(name="name")
	private String name;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> screens;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> groups;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> schedules;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> diffusers;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> fluxes;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> members;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> admins;

	public Team() {
	}

	public Team(String name) {
		this.name = name;

		this.fluxes = new HashSet<>();
		this.diffusers = new HashSet<>();
		this.schedules = new HashSet<>();
		this.groups = new HashSet<>();
		this.screens = new HashSet<>();
		this.members = new HashSet<>();
	}

	public Team(TeamData data) {
		name = data.getName();
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

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getGroups() {
		return groups;
	}

	public void setGroups(Set<Integer> screenGroups) {
		this.groups = screenGroups;
	}

	public void addToScreenGroups(Integer group) {
		this.groups.add(group);
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getSchedules() {
		return schedules;
	}

	public void setSchedules(Set<Integer> schedules) {
		this.schedules = schedules;
	}

	public void addToSchedules(Integer schedule) {
		if (this.schedules == null)
			this.schedules = new HashSet<>();
		this.schedules.add(schedule);
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getDiffusers() {
		return diffusers;
	}

	public void setDiffusers(Set<Integer> diffusers) {
		this.diffusers = diffusers;
	}

	public void addToDiffusers(Integer diffuser) {
		this.diffusers.add(diffuser);
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getFluxes() {
		return fluxes;
	}

	public void setFluxes(Set<Integer> fluxes) {
		this.fluxes = fluxes;
	}

	public void addToFluxes(Integer flux) {
		this.fluxes.add(flux);
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getScreens() {
		return screens;
	}

	public void setScreens(Set<Integer> screens) {
		this.screens = screens;
	}

	public void addScreen(Integer s) {
		if (screens == null)
			screens = new HashSet<>();
		screens.add(s);
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getMembers() {
		return members;
	}

	public void setMembers(Set<Integer> members) {
		this.members = members;
	}

	public void addMember(Integer m) {
		if (members == null)
			members = new HashSet<>();
		members.add(m);
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getAdmins() {
		return admins;
	}

	public void setAdmins(Set<Integer> admins) {
		this.admins = admins;
	}

	public void addAdmin(Integer a) {
		if (admins == null)
			admins = new HashSet<>();
		admins.add(a);
	}
}
