package models.entities;

import models.db.Diffuser;

import java.util.List;

public class DiffuserData {

	private String name;
	private String fluxName;
	private String startTime;
	private String validity;
	private List<String> screens;

	public DiffuserData() {
	}

	public DiffuserData(String name) {
		this.name = name;
	}

	public DiffuserData(Diffuser diffuser) {
		name = diffuser.getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFluxName() {
		return fluxName;
	}

	public void setFluxName(String fluxName) {
		this.fluxName = fluxName;
	}

	public List<String> getScreens() {
		return screens;
	}

	public void setScreens(List<String> screens) {
		this.screens = screens;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getValidity() {
		return validity;
	}

	public void setValidity(String validity) {
		this.validity = validity;
	}
}
