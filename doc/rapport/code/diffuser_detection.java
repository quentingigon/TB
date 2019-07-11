private void sendFluxEvent(FluxEvent event, Integer scheduleId) {
	Flux currentFlux = getFlux(event.getFluxId());

	if (currentFlux != null) {
		// envoie le flux diffuse aux ecrans concernes et retourne les ids des autres ecrans
		String screenIdsWithNoActiveDiffuser = sendDiffusedFluxToConcernedScreens(event, scheduleId);

		if (!screenIdsWithNoActiveDiffuser.isEmpty()) {
			List<String> macAddresses = getMacAddresses(screenIdsWithNoActiveDiffuser);
			// envoie l'event prevu aux ecrans restants
			send(currentFlux, macAddresses);
		}
	}
}