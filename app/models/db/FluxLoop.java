package models.db;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name="fluxloop", schema="public")
public class FluxLoop {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Integer id;

	@Column(name="schedule_id")
	private Integer scheduleId;

	@Column(name="time")
	private String startTime;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Integer> fluxes;

	public FluxLoop() {
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@ElementCollection(fetch = FetchType.EAGER)
	public Set<Integer> getFluxes() {
		return fluxes;
	}

	public void setFluxes(Set<Integer> fluxes) {
		this.fluxes = fluxes;
	}

	public void addToFluxes(Integer flux) {
		this.fluxes.add(flux);
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public Integer getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Integer scheduleId) {
		this.scheduleId = scheduleId;
	}
}
