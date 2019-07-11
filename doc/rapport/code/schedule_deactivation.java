public Result deactivate(String name, Http.Request request) {
	ScheduleService scheduleService = servicePicker.getScheduleService();
	Schedule schedule = scheduleService.getScheduleByName(name);
	
	RunningSchedule rs = scheduleService.getRunningScheduleByScheduleId(schedule.getId());

	// supprimme de la BD
	scheduleService.delete(rs);

	// supprime les jobs associes a ce schedule (par nom)
	deleteJobsOfSchedule(schedule);

	return index(request);	
}