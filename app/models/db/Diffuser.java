package models.db;

import javax.persistence.*;

/**
 * This class is the DM model for Diffuser.
 */
@Entity
@Table(name="diffuser", schema="public")
public class Diffuser {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="diffuser_id")
	private Integer id;

	@Column(name="name")
	private String name;

	@Column(name="validity")
	private Integer validity;

	@Column(name="flux_id")
	private Integer flux;

	@Column(name="cron_cmd")
	private String cronCmd;

	@Column(name="days")
	private String days;

	public Diffuser() {
	}

	public Diffuser(String name) {
		this.name = name;
	}

	// Getters and setters

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getId() {
		return id;
	}

	public Integer getValidity() {
		return validity;
	}

	public void setValidity(Integer validity) {
		this.validity = validity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getFlux() {
		return flux;
	}

	public void setFlux(Integer flux) {
		this.flux = flux;
	}

	public String getCronCmd() {
		return cronCmd;
	}

	public void setCronCmd(String cronCmd) {
		this.cronCmd = cronCmd;
	}

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}
}
