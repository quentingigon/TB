public Result deactivate(Http.Request request, String name) {
	Diffuser diffuser = diffuserService.getDiffuserByName(name);
	RunningDiffuser rd = diffuserService.getRunningDiffuserByDiffuserId(diffuser.getId());

	// recuperation des ecrans concernes par la diffusion
	for (Integer id: diffuserService.getScreenIdsOfRunningDiffuserById(rd.getId())) {
		Screen screen = screenService.getScreenById(id);

		// si l'ecran est actif, on met a jour le Schedule associe
		if (screen.getRunningScheduleId() != null) {
			RunningSchedule rs = scheduleService.getRunningScheduleById(screen.getRunningScheduleId());

			Schedule schedule = scheduleService.getScheduleById(rs.getScheduleId());
			schedule.removeFromFluxes(diffuser.getFlux());

			scheduleService.update(schedule);
		}

		// on enleve le flux de l'horaire du Schedule associe
		RunningScheduleThread rst = threadManager.getServiceByScheduleId(id);
		if (rst != null) {
			rst.removeScheduledFluxFromDiffuser(
				fluxService.getFluxById(diffuser.getFlux()),
				diffuser.getId(),
				diffuser.getStartBlock()
			);
		}
	}
	// suppression du RunningDiffuser
	diffuserService.delete(rd);

	return index(request);
}