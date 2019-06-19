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

	@Column(name="cron_cmd")
	private String cronCmd;

	public FluxTrigger() {
	}

	public FluxTrigger(Integer fluxId, String cronCmd) {
		this.fluxId = fluxId;
		this.cronCmd = cronCmd;
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

	public String getCronCmd() {
		return cronCmd;
	}

	public void setCronCmd(String cronCmd) {
		this.cronCmd = cronCmd;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}
}
