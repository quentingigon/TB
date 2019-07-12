public Result deactivate(Http.Request request, String mac) {
    Screen screen = screenService.getScreenByMacAddress(mac);
    Integer rsId = scheduleService.getRunningScheduleOfScreenById(screen.getId());
    RunningSchedule rs = scheduleService.getRunningScheduleById(rsId);
    // si l'ecran est actif
    if (rs != null) {
        // supprime l'ecran de la liste des ecrans concernes par le schedule
        List<Integer> screenIds = scheduleService.getAllScreenIdsOfRunningScheduleById(rs.getId());
        screenIds.remove(screen.getId());
        rs.setScreens(screenIds);
    }
    scheduleService.update(rs);
    return index(request);
}