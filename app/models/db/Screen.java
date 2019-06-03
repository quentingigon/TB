package models.db;

import javax.persistence.*;

/**
 * This class is the DM model for Screen.
 */
@Entity
@Table(name="screen", schema = "public")
public class Screen {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="screen_id")
	private Integer id;

	private String name;

	@Column(name="mac_address")
	private String macAddress;

	@Column(name="site_id")
	private Integer siteId;

	@Column(name="runningschedule_id")
	private Integer runningscheduleId;

	@Column(name="next_to")
	private Integer screenNextTo;

	@Column(name="resolution")
	private String resolution;

	@Column(name="current_flux_name")
	private String currentFluxName;

	@Column(name="logged")
	private boolean logged;

	@Column(name="active")
	private boolean active;

	public Screen(String macAddress) {
		this.macAddress = macAddress;
		logged = false;
	}

	public Screen() {

	}

	// Getters and setters

	public Integer getId() {
		return id;
	}

	public String getMacAddress() {
		return macAddress;
	}

	public void setMacAddress(String macAddress) {
		this.macAddress = macAddress;
	}

	public Integer getSiteId() {
		return siteId;
	}

	public void setSiteId(Integer site_id) {
		this.siteId = site_id;
	}

	public boolean isLogged() {
		return logged;
	}

	public void setLogged(boolean logged) {
		this.logged = logged;
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

	public Integer getRunningScheduleId() {
		return runningscheduleId;
	}

	public void setRunningscheduleId(Integer runningscheduleId) {
		this.runningscheduleId = runningscheduleId;
	}

	public Integer getScreenNextTo() {
		return screenNextTo;
	}

	public void setScreenNextTo(Integer screenNextTo) {
		this.screenNextTo = screenNextTo;
	}

	public Integer getRunningscheduleId() {
		return runningscheduleId;
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
}
