// point d'acces pour Diffusers
public void createJobForDiffuser(Diffuser diffuser,
                                 Flux diffusedFlux,
                                 List<Screen> screens) {
    createJob("diffuser",
        diffuser.getName(),
        diffuser.getId(),
        getCronCmdDiffuser(diffuser, diffuser.getTime()),
        diffuser.getTime(),
        diffusedFlux.getId(),
        screens,
        DIFFUSER_JOBS_LISTENER);
}
// point d'acces pour Schedules
public void createJobForSchedule(Schedule schedule,
                                 FluxTrigger fluxTrigger,
                                 List<Screen> screens) {
    createJob("schedule",
        schedule.getName(),
        schedule.getId(),
        getCronCmdSchedule(schedule, fluxTrigger.getTime()),
        fluxTrigger.getTime(),
        fluxTrigger.getFluxId(),
        screens,
        SCHEDULE_JOBS_LISTENER);
}

// fonction de creation de SendEventJob
private void createJob(String source, String name, Integer entityId, String cronCmd,
                       String time, Integer fluxId, List<Screen> screens, String jobListenerName) {
    FluxService fluxService = servicePicker.getFluxService();
    Flux flux = fluxService.getFluxById(fluxId);
    // creation du job
    JobDetail job = newJob(SendEventJob.class)
        .withIdentity(JOB_NAME_TRIGGER + flux.getName() + "#" + cronCmd,
            SEND_EVENT_GROUP + "." + source + "." + name)
        .build();
    // creation du trigger
    CronTrigger trigger = newTrigger()
        .withIdentity(TRIGGER_NAME + flux.getName() + "#" + cronCmd,
            SEND_EVENT_GROUP + "." + source + "." + name)
        .usingJobData("screenIds", getScreenIds(screens))
        .usingJobData("fluxId", flux.getId())
        .usingJobData("source", source)
        .usingJobData("time", time)
        .usingJobData("entityId", entityId)
        .withSchedule(cronSchedule(cronCmd))
        .build();

    SchedulerFactory sf = new StdSchedulerFactory();
    try {
        Scheduler scheduler = sf.getScheduler();

        // suppression d'un job ayant potentiellement le meme nom
        if (scheduler.checkExists(new JobKey(JOB_NAME_TRIGGER + name, SEND_EVENT_GROUP))) {
            scheduler.deleteJob(new JobKey(JOB_NAME_TRIGGER + name, SEND_EVENT_GROUP));
        }
        // scheduling du job
        scheduler.scheduleJob(job, trigger);

        // si il n'y a pas encore de listener pour ce type de job
        if (scheduler.getListenerManager().getJobListener(jobListenerName) == null) {
            SendEventJobsListener listener = new SendEventJobsListener(
                jobListenerName,
                eventManager,
                servicePicker);
            // creation et demarrage du listener pour les jobs faisant partie du bon groupe
            scheduler.getListenerManager().addJobListener(listener,
                GroupMatcher.jobGroupContains(SEND_EVENT_GROUP + "." + source));
        }
    } catch (SchedulerException e) {
        e.printStackTrace();
    }
}