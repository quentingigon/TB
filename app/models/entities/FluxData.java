package models.entities;

import models.db.Flux;

public class FluxData {

	private String name;
	private String url;
	private String duration;
	private String type;

	public FluxData() {
	}

	public FluxData(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public FluxData(String name, String url, String duration, String type) {
		this.name = name;
		this.url = url;
		this.duration = duration;
		this.type = type;
	}

	public FluxData(Flux flux) {
		this.name = flux.getName();
		this.duration = String.valueOf(flux.getDuration());
		this.url = flux.getUrl();
		this.type = flux.getType();
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

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
