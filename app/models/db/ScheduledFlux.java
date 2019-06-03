package models.db;

import javax.persistence.*;

/**
 * This class is the DM model for ScheduledFlux.
 */
@Entity
@Table(name="scheduled_flux", schema = "public")
public class ScheduledFlux {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="scheduled_flux_id")
	private Integer id;

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

	// Getters and setters

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
