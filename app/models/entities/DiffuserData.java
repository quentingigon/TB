package models.entities;

import models.db.Diffuser;

import java.util.List;

public class DiffuserData {

	private String name;
	private String fluxName;
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
}
