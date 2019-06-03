public Result deactivate(Http.Request request, String mac) {
    Screen screen = servicePicker.getScreenService().getScreenByMacAddress(mac);
    Integer rsId = scheduleService.getRunningScheduleOfScreenById(screen.getId());
    RunningSchedule rs = scheduleService.getRunningScheduleById(rsId);

    // ecran actif
    if (rs != null) {
        // enleve l'ecran courant du RunningSchedule concerne
        List<Integer> screenIds = 
            scheduleService.getAllScreenIdsOfRunningScheduleById(rs.getId());
        screenIds.remove(screen.getId());
        rs.setScreens(screenIds);

        RunningScheduleThread rst = 
            threadManager.getServiceByScheduleId(rs.getScheduleId());

        List<Screen> screenList =new ArrayList<>();
        for (Integer screenId: screenIds) {
            screenList.add(screenService.getScreenById(screenId));
        }
        // stop l'ancien thread
        rst.abort();
        threadManager.removeRunningSchedule(rs.getScheduleId());

        Schedule schedule = scheduleService.getScheduleById(rs.getScheduleId());

        // on cree un nouveau thread a partir de l'ancien 
        RunningScheduleThread task = new RunningScheduleThread(
            rs,
            screenList,
            new ArrayList<>(schedule.getFluxes()),
            rst.getTimetable(),
            fluxRepository,
            fluxChecker,
            schedule.isKeepOrder());

        task.addObserver(fluxManager);
        threadManager.addRunningScheduleThread(rs.getScheduleId(), task);

    }
    // mise a jour du RunningSchedule associe
    scheduleService.update(rs);
    
    return index(request);
}