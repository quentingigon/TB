package models.db;

import javax.persistence.*;

@Entity
@Table(name="fluxtrigger", schema = "public")
public class FluxTrigger {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;

	@Column(name="schedule_id")
	private Integer scheduleId;

	@Column(name="flux_id")
	private Integer fluxId;

	@Column(name="time")
	private String time;

	public FluxTrigger() {
	}

	public FluxTrigger(Integer fluxId) {
		this.fluxId = fluxId;
	}

	public FluxTrigger(String time, Integer fluxId, Integer scheduleId) {
		this.fluxId = fluxId;
		this.time = time;
		this.scheduleId = scheduleId;
	}

	public Integer getId() {
		return id;
	}

	public Integer getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Integer scheduleId) {
		this.scheduleId = scheduleId;
	}

	public Integer getFluxId() {
		return fluxId;
	}

	public void setFluxId(Integer fluxId) {
		this.fluxId = fluxId;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
