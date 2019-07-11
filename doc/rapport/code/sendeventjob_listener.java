public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
    JobDataMap triggerDataMap = context.getTrigger().getJobDataMap();
    SendEventJob job = (SendEventJob) context.getJobInstance();
    // verification si le flux a des informations a afficher
    checkFlux(context.getJobDetail().getKey().toString(), job);

    lastJob = job;
    lastFluxHadNothingToDisplay = currentFluxHasNothingToDisplay;
    // envoi de l'evenement a l'EventManager
    eventManager.handleEvent(job, currentFluxHasNothingToDisplay);

    List<FluxTrigger> triggers = servicePicker.getFluxService().getFluxTriggersOfScheduleById(job.getEntityId());
    triggers.sort(Comparator.comparing(FluxTrigger::getTime));

    List<FluxLoop> loops = servicePicker.getFluxService().getFluxLoopOfScheduleById(job.getEntityId());
    loops.sort(Comparator.comparing(FluxLoop::getStartTime));

    FluxEvent event = job.getEvent();
    Schedule schedule = servicePicker.getScheduleService().getScheduleById(job.getEntityId());
    
    // calcul de l'heure apres l'execution de l'evenement
    Flux currentFlux = servicePicker.getFluxService().getFluxById(event.getFluxId());
    DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
    LocalTime timeAfterExecution = formatter.parseLocalTime(triggerDataMap.getString("time"))
        .plusMinutes(currentFlux.getTotalDuration());

    boolean isCurrentTriggerLast = true;
    for (FluxLoop loop: loops) {
        // si un FluxLoop est programme pour l'heure calculee juste en dessus
        if (mustFluxLoopBeStarted(formatter.print(timeAfterExecution), loop, triggers)) {
            LoopEventJobCreator loopJobCreator = new LoopEventJobCreator(schedule, event.getScreenIds(), servicePicker, eventManager);
            loopJobCreator.createFromFluxLoop(loop);
            isCurrentTriggerLast = false;
        }
    }
    // si le job represente la derniere entree du Schedule, on envoie la premiere loop du Schedule
    if (isCurrentTriggerLast && !isThereFluxTriggersAfterTime(formatter.print(timeAfterExecution), triggers)) {
        if (!loops.isEmpty()) {
            LoopEventJobCreator loopJobCreator = new LoopEventJobCreator(schedule, event.getScreenIds(), servicePicker, eventManager);
            loopJobCreator.createFromFluxLoop(loops.get(0));
        }
    }
}