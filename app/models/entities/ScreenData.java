package models.entities;

import models.db.Screen;

/**
 * This class represents the Screen data being sent between clients and server.
 */
public class ScreenData {

	private String name;
	private String mac;
	private String code;
	private String site;
	private String resolution;
	private String currentFluxName;

	private boolean logged;
	private boolean active;

	public ScreenData() {
	}

	public ScreenData(String mac) {
		this.mac = mac;
	}

	public ScreenData(Screen s) {
		this.name = s.getName();
		this.mac = s.getMacAddress();
		this.site = String.valueOf(s.getSiteId());
		this.resolution = s.getResolution();
		this.active = s.isActive();
		this.logged = s.isLogged();
	}

	// Getters and setters

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getResolution() {
		return resolution;
	}

	public void setResolution(String resolution) {
		this.resolution = resolution;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public String getCurrentFluxName() {
		return currentFluxName;
	}

	public void setCurrentFluxName(String currentFluxName) {
		this.currentFluxName = currentFluxName;
	}

	public boolean isLogged() {
		return logged;
	}

	public void setLogged(boolean logged) {
		this.logged = logged;
	}
}
