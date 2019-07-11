public void handleEvent (EventJob job, boolean fluxHasNothingToDisplay) {
    FluxEvent event = job.getEvent();

    // si le flux n'a rien a afficher, envoi d'un flux de fallback a la place
    if (fluxHasNothingToDisplay && job.isJobFromSchedule()) {
        Flux fallback = getRandomFallBackFlux(job.getEntityId());
        if (fallback != null) {
            event = new FluxEvent(fallback.getId(), event.getScreenIds());
        }
    }

    // si le job vient d'un schedule
    if (job.isJobFromSchedule()) {
        sendFluxEventAsGeneralOrLocated(event, job.getEntityId());
    }
    // si le job vient d'un diffuser
    else if (!fluxHasNothingToDisplay) {
        sendFluxEvent(event, job.getEntityId());
    }
}