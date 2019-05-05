package models.db;

import models.entities.FluxData;

import javax.persistence.*;

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

	@Column(name="url")
	private String url;

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
		duration = Long.valueOf(fluxData.getDuration());
		url = fluxData.getUrl();
		type = fluxData.getType();
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
}
