package models.db;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="runningschedule", schema="public")
public class RunningSchedule {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="runningschedule_id")
	private Integer id;

	@Column(name="schedule_id")
	private Integer scheduleId;

	@ManyToMany
	@JoinColumn(name="screens", nullable = false)
	private List<Screen> screens;


	public RunningSchedule() {
		super();
	}

	public RunningSchedule(Schedule schedule) {
		// this.name = schedule.getName();
		this.screens = new ArrayList<>();
		// this.fluxes = new ArrayList<>();
	}

	public List<Screen> getScreens() {
		return screens;
	}

	public void setScreens(List<Screen> screens) {
		this.screens = screens;
	}

	public void addToScreens(Screen s) {
		if (screens == null)
			screens = new ArrayList<>();
		screens.add(s);
	}

	public Integer getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(Integer scheduleId) {
		this.scheduleId = scheduleId;
	}
}
