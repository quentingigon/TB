package models.db;

import javax.persistence.*;

@Entity
@Table(name="scheduled_flux", schema = "public")
public class ScheduledFlux {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="scheduled_flux_id")
	private Integer id;

	// TODO maybe associate with RunningSchedule instead of Schedule -> big refactor
	@Column(name="schedule_id")
	private Integer scheduleId;

	@Column(name="flux_id")
	private Integer fluxId;

	@Column(name="start_block")
	private Integer startBlock;

	public ScheduledFlux() {
	}

	public ScheduledFlux(Integer scheduleId, Integer fluxId, Integer startBlock) {
		this.scheduleId = scheduleId;
		this.fluxId = fluxId;
		this.startBlock = startBlock;
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

	public Integer getStartBlock() {
		return startBlock;
	}

	public void setStartBlock(Integer startBlock) {
		this.startBlock = startBlock;
	}
}
