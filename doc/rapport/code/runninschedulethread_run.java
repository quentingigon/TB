public void run() {
  while (running) {

    DateTime dt = new DateTime();
    int hours = dt.getHourOfDay();
    int minutes = dt.getMinuteOfHour();

    // si on est dans la plage horaire active
    if (hours >= beginningHour && hours < endHour) {
      int blockIndex = getBlockNumberOfTime(hours, minutes);

      do {
        Flux currentFlux = fluxRepository.getById(timetable.get(blockIndex++));

        // si un flux est prevu pour ce bloc
        if (currentFlux != null) {
          sendFluxEventAsGeneralOrLocated(currentFlux);
        }
        // sinon on choisit un flux sans heure de debut
        else if (!unscheduledFluxIds.isEmpty()) {
          sendUnscheduledFlux(blockIndex);
        }
        // sinon on choisit un flux de fallback
        else if (!fluxRepository.getAllFallbackIdsOfSchedule(runningSchedule.getScheduleId()).isEmpty()) {
          sendFallbackFlux(blockIndex);
        }
        // sinon on envoie le flux d'erreur
        else {
          sendFluxEvent(fluxRepository.getByName(WAIT_FLUX), screens);
        }
        // sleep pendant 1 minutes
        try {
          if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException("Thread interrupted");
          }
          Thread.sleep((long) blockDuration * 60000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }

      } while (blockIndex < blockNumber && running);
    }
  }
}