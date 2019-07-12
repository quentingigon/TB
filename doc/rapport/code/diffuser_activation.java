// activation
public Result activate(Http.Request request) throws SchedulerException {
	final Form<DiffuserData> boundForm = form.bindFromRequest(request);
	DiffuserData data = boundForm.get();
	Diffuser diffuser = diffuserService.getDiffuserByName(data.getName());

	// ids des ecrans concernes par le diffuser
	Set<Integer> screenIds = new HashSet<>();
	List<Screen> screens = new ArrayList<>();
	for (String mac: data.getScreens()) {
		Screen screen = screenService.getScreenByMacAddress(mac);
		screenIds.add(screen.getId());
		screens.add(screen);
	}

	// creation du diffuser
	Flux diffusedFlux = fluxService.getFluxById(diffuser.getFlux());
	RunningDiffuser rd = new RunningDiffuser(diffuser);
	rd.setDiffuserId(diffuser.getId());
	rd.setScreens(new ArrayList<>(screenIds));
	diffuserService.create(rd);

	// liste des ecrans sans Schedule actif
	List<Screen> screensWithNoRunningSchedule = new ArrayList<>();
	for (Screen screen: screens) {
		Integer runningScheduleId = scheduleService.getRunningScheduleOfScreenById(screen.getId());
		if (runningScheduleId == null) {
			screensWithNoRunningSchedule.add(screen);
		}
	}
	// creation d'un job et trigger pour tout les ecrans sans Schedule actif
	if (!screensWithNoRunningSchedule.isEmpty()) {
		SendEventJobCreator jobCreator = new SendEventJobCreator(servicePicker,eventManager);
		jobCreator.createJobForDiffuser(diffuser, diffusedFlux,screensWithNoRunningSchedule);
	}
	return index(request);
}

// desactivation
public Result deactivate(Http.Request request, String name) {
	Diffuser diffuser = diffuserService.getDiffuserByName(name);
	RunningDiffuser rd = diffuserService.getRunningDiffuserByDiffuserId(diffuser.getId());
	diffuserService.delete(rd); // suppression du RunningDiffuser

	SchedulerFactory sf = new StdSchedulerFactory();
	Scheduler scheduler;
	try {
		scheduler = sf.getScheduler();
		Flux flux = fluxService.getFluxById(diffuser.getFlux());
		String jobName = JOB_NAME_TRIGGER + flux.getName() + "#" + getCronCmdDiffuser(diffuser, diffuser.getTime());
		scheduler.deleteJob(new JobKey(jobName, diffuser.getName())); // suppression du job
	} catch (SchedulerException e) {
		e.printStackTrace();
	}
	return index(request);
}