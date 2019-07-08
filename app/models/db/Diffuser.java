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

	@Column(name="flux_id")
	private Integer flux;

	@Column(name="days")
	private String days;

	@Column(name="time")
	private String time;

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

	public String getDays() {
		return days;
	}

	public void setDays(String days) {
		this.days = days;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
