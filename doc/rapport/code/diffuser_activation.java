// activation
public Result activate(Http.Request request) {
	final Form<DiffuserData> boundForm = form.bindFromRequest(request);
	DiffuserData data = boundForm.get();
	Diffuser diffuser = diffuserService.getDiffuserByName(data.getName());

	// ids des ecrans concernes par le diffuser
	Set<Integer> screenIds = new HashSet<>();
	for (String mac: data.getScreens()) {
		Screen screen = screenService.getScreenByMacAddress(mac);
		if (screen == null) {
			return activateViewWithErrorMessage(data.getName(), request, "Screen MAC address does not exists");
		}
		screenIds.add(screen.getId());
	}

	// creation du diffuser
	Flux diffusedFlux = fluxService.getFluxById(diffuser.getFlux());
	RunningDiffuser rd = new RunningDiffuser(diffuser);
	rd.setDiffuserId(diffuser.getId());
	rd.setScreens(new ArrayList<>(screenIds));
	rd.setFluxId(diffusedFlux.getId());
	diffuserService.create(rd);

	return index(request);
}

// desactivation
public Result deactivate(Http.Request request, String name) {
	Diffuser diffuser = diffuserService.getDiffuserByName(name);

	RunningDiffuser rd = diffuserService.getRunningDiffuserByDiffuserId(diffuser.getId());
	diffuserService.delete(rd);
	return index(request);
}