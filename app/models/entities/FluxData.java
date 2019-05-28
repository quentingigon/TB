package models.entities;

import models.db.Flux;

public class FluxData {

	private String name;
	private String url;
	private String dataCheckUrl;
	private String duration;
	private String numberOfPhases;
	private String type;
	private String site;
	private String team;
	private String startTime;

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
		this.numberOfPhases = String.valueOf(flux.getNumberOfPhases());

		if (flux.getDataCheckUrl() != null)
			this.dataCheckUrl = flux.getDataCheckUrl();

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

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getTeam() {
		return team;
	}

	public void setTeam(String team) {
		this.team = team;
	}

	public String getDataCheckUrl() {
		return dataCheckUrl;
	}

	public void setDataCheckUrl(String dataCheckUrl) {
		this.dataCheckUrl = dataCheckUrl;
	}

	public String getNumberOfPhases() {
		return numberOfPhases;
	}

	public void setNumberOfPhases(String numberOfPhases) {
		this.numberOfPhases = numberOfPhases;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}
}
