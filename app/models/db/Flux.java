package models.db;

import models.entities.FluxData;

import javax.persistence.*;

/**
 * This class is the DM model for Flux.
 */
@Entity
@Table(name="flux", schema="public")
public class Flux {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="flux_id")
	private Integer id;

	@Column(name="name")
	private String name;

	@Column(name="phase_duration")
	private long duration;

	@Column(name="phase_n")
	private long numberOfPhases;

	@Column(name="url")
	private String url;

	@Column(name="type")
	private String type;

	public Flux(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public Flux(String name, long duration, String url) {
		this.name = name;
		this.duration = duration;
		this.url = url;
	}

	public Flux() {
	}

	public Flux(FluxData fluxData) {
		name = fluxData.getName();
		duration = Long.parseLong(fluxData.getDuration());
		if (fluxData.getNumberOfPhases() != null)
			numberOfPhases = Long.parseLong(fluxData.getNumberOfPhases());
		else
			numberOfPhases = 1;
		url = fluxData.getUrl();
		type = fluxData.getType();
	}

	// Getters and setters

	public Integer getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public long getNumberOfPhases() {
		return numberOfPhases;
	}

	public void setNumberOfPhases(long numberOfPhases) {
		this.numberOfPhases = numberOfPhases;
	}

	public long getTotalDuration() {
		return numberOfPhases * duration;
	}
}
