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
        // sinon on choisi un flux sans heure de debut
        else {
          int freeBlocksN = getNumberOfBlocksToNextScheduledFlux(blockIndex);
          boolean sent = false;
          int fluxId;

          do {
            // s'il a ete specifie que l'ordre des flux devait etre respecte
            if (keepOrder) {
              // cycle
              unscheduledFluxIds.add(unscheduledFluxIds.get(0));
              fluxId = unscheduledFluxIds.remove(0);
            }
            // sinon on en prend un au hasard
            else {
              Collections.shuffle(unscheduledFluxIds);
              fluxId = unscheduledFluxIds.get(0);
            }
            Flux unscheduledFlux = fluxRepository.getById(fluxId);

            // si la place est suffisante
            if (unscheduledFlux.getTotalDuration() <= freeBlocksN) {
              // mise a jour de l'horaire
              scheduleFlux(unscheduledFlux, blockIndex);
              // envoi de l'event au FluxManager
              sendFluxEventAsGeneralOrLocated(unscheduledFlux);
              sent = true;
            }
          } while (!sent);
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