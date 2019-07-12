public Result activate(Http.Request request) {
	final Form<ScheduleData> boundForm = form.bindFromRequest(request);
	ScheduleData data = boundForm.get();
	Schedule schedule = scheduleService.getScheduleByName(data.getName());
	
	// On cree le RunningSchedule
	RunningSchedule rs = new RunningSchedule(schedule);
	rs = scheduleService.create(rs);

	// creation de la liste des ecrans concernes par le Schedule
	List<Screen> screens = new ArrayList<>();
	for (String screenMac : data.getScreens()) {
		Screen screen = screenService.getScreenByMacAddress(screenMac);
		rs.addToScreens(screen.getId());
		screen.setRunningscheduleId(rs.getId());
		screen.setActive(true);
		screens.add(screen);
		screenService.update(screen);
	}
	scheduleService.update(rs);

	// creation des jobs et triggers necessaires
	createJobsForSchedule(scheduleToActivate, screens);
	return index(request);
}