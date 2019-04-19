package models.db;

import java.util.List;

public class RunningSchedule extends Schedule {

	private List<Screen> screens;

	public RunningSchedule() {
	}

	public List<Screen> getScreens() {
		return screens;
	}

	public void setScreens(List<Screen> screens) {
		this.screens = screens;
	}
}
