public Result deactivate(String name, Http.Request request) {
	ScheduleService scheduleService = servicePicker.getScheduleService();
	Schedule schedule = scheduleService.getScheduleByName(name);
	
	RunningSchedule rs = scheduleService.getRunningScheduleByScheduleId(schedule.getId());

	// supprimme de la BD
	scheduleService.delete(rs);

	// stop le thread correspondant
	threadManager.removeRunningSchedule(schedule.getId());

	return index(request);	
}