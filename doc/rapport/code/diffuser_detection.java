private void sendFluxEvent(Flux flux, List<Screen> screenList) {
	// verifie si un Diffuser est actif pour un des ecran de la liste
	// si oui, envoie le flux diffuse et enleve l'ecran de la liste
	for (Screen screen: screenList) {
		Integer rdId = diffuserService.getRunningDiffuserIdByScreenId(screen.getId());

		if (rdId != null) {
			RunningDiffuser rd = diffuserService.getRunningDiffuserById(rdId);
			List<Screen> screens = new ArrayList<>();
			screens.add(screen);
			screenList.remove(screen);
			FluxEvent diffusedEvent = new FluxEvent(fluxService.getFluxById(rd.getFluxId()), screens);
			setChanged();
			notifyObservers(diffusedEvent);
		}
	}

	// envoie l'event prevu aux ecrans restants
	FluxEvent event = new FluxEvent(flux, screenList);
	lastFluxEvent = event;
	setChanged();
	notifyObservers(event);
}