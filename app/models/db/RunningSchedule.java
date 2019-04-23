package models.db;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

public class RunningSchedule extends Schedule {

	@ManyToMany
	@JoinColumn(name="screens", nullable = false)
	private List<Screen> screens;

	public RunningSchedule() {
		super();
	}

	public RunningSchedule(Schedule schedule) {
		this.name = schedule.getName();
		this.fluxes = schedule.getFluxes();
		this.screens = new ArrayList<>();
	}

	public List<Screen> getScreens() {
		return screens;
	}

	public void setScreens(List<Screen> screens) {
		this.screens = screens;
	}
}
