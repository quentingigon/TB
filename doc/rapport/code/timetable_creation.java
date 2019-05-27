public HashMap<Integer, Integer> getTimeTable(Schedule schedule) {
    List<ScheduledFlux> scheduledFluxes = scheduleRepository.getAllScheduledFluxesByScheduleId(schedule.getId());
    Flux lastFlux = new Flux();
    long lastFluxDuration = 0;
    boolean noFluxSent;

    HashMap<Integer, Integer> timetable = new HashMap<>();
    for (int i = 0; i < blockNumber; i++) {
      noFluxSent = true;

      if (lastFluxDuration != 0) {
        lastFluxDuration--;
        timetable.put(i, lastFlux.getId());
      }
      else {
        for (ScheduledFlux sf : scheduledFluxes) {
          // si un flux doit commencer a ce bloc
          if (sf.getStartBlock().equals(i)) {
            Flux flux = fluxRepository.getById(sf.getFluxId());
            lastFlux = flux;
            lastFluxDuration = flux.getTotalDuration() - 1;
            timetable.put(i, flux.getId());
            noFluxSent = false;
            scheduledFluxes.remove(sf);
            break;
          }
        }
        if (noFluxSent) {
          // aucun flux ne commence a ce bloc
          timetable.put(i, -1);
        }
      }
    }
    return timetable;
  }