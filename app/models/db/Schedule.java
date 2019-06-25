package models.db;

import models.entities.ScheduleData;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is the DM model for Schedule.
 */
@Entity
@Table(name="schedule", schema="public")
public class Schedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="schedule_id")
	private Integer id;

	@Column(name="name")
	private String name;

	@Column(name="days")
	private String days;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> fluxtriggers;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> fluxes;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> fallbacks;

	public Schedule() {
		fallbacks = new HashSet<>();
		fluxes = new HashSet<>();
	}

	public Schedule(String name) {
		this.name = name;
		fallbacks = new HashSet<>();
	}

	public Schedule(ScheduleData data) {
		this.name = data.getName();
		this.fluxes = new HashSet<>();
		this.fallbacks = new HashSet<>();
	}

	// Getters and setters

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getFluxes() {
		return fluxes;
	}

	public void setFluxes(Set<Integer> fluxes) {
		this.fluxes = fluxes;
	}

	public void addToFluxes(Integer fluxId) {
		if (this.fluxes == null) {
			this.fluxes = new HashSet<>();
		}
		this.fluxes.add(fluxId);
	}

	public void removeFromFluxes(Integer fluxId) {
		if (this.fluxes == null) {
			this.fluxes = new HashSet<>();
		}
		this.fluxes.remove(fluxId);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getFallbacks() {
		return fallbacks;
	}

	public void setFallbacks(Set<Integer> fallbacks) {
		this.fallbacks = fallbacks;
	}

	public void addToFallbacks(Integer fluxId) {
		if (this.fallbacks == null) {
			this.fallbacks = new HashSet<>();
		}
		this.fallbacks.add(fluxId);
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getFluxtriggers() {
		return fluxtriggers;
	}

	public void setFluxtriggers(Set<Integer> fluxtrigger) {
		this.fluxtriggers = fluxtrigger;
	}

	public void addToFluxtriggers(Integer ftId) {
		if (this.fluxtriggers == null) {
			this.fluxtriggers = new HashSet<>();
		}
		this.fluxtriggers.add(ftId);
	}
}
