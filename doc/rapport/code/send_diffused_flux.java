private String sendDiffusedFluxToConcernedScreens(FluxEvent event, Integer scheduleId) {
    StringBuilder screenIdsWithNoDiffuser = new StringBuilder();
    List<Screen> screens = getScreenList(event.getScreenIds());
    DiffuserService diffuserService = servicePicker.getDiffuserService();

    for (Screen s: screens) {
        RunningDiffuser rd = getRunningDiffuserIfPresent(s);
        // l'ecran a un Diffuser actif
        if (rd != null) {
            Diffuser diffuser = diffuserService.getDiffuserById(rd.getDiffuserId());
            // l'ecran a un Schedule actif
            if (scheduleId != null) {
                Schedule schedule = servicePicker.getScheduleService().getScheduleById(scheduleId);

                // si l'heure de diffusion du Diffuser est maintenant
                if (schedule != null &&
                    (checkIfScheduleAndDiffuserDaysOverlap(schedule, diffuser)
                    && checkIfScheduleAndDiffuserTimeOverlap(schedule, diffuser))) {
                    send(servicePicker.getFluxService().getFluxById(diffuser.getFlux()),
                        getMacAddresses(event.getScreenIds()));
                }
            }
            // pas de Schedule, juste un Diffuser actif
            else {
                send(servicePicker.getFluxService().getFluxById(diffuser.getFlux()),
                    getMacAddresses(event.getScreenIds()));
            }
        }
        else {
            screenIdsWithNoDiffuser.append(s.getId());
        }
    }
    // retourne les ids des ecrans sans Diffuser actif
    return screenIdsWithNoDiffuser.toString();
}