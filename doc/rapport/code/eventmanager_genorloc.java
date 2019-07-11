private void sendFluxEventAsGeneralOrLocated(FluxEvent fluxEvent, int scheduleId) {
    FluxService fluxService = servicePicker.getFluxService();
    Flux currentFlux = fluxService.getFluxById(fluxEvent.getFluxId());

    // si le flux courant est localise
    if (fluxService.getLocatedFluxByFluxId(currentFlux.getId()) != null) {
      int siteId = fluxService.getLocatedFluxByFluxId(currentFlux.getId()).getSiteId();
      List<String> screenIds = new ArrayList<>(Arrays.asList(fluxEvent.getScreenIds().split(",")));

      // recupere les ids des ecrans qui ne sont pas localise sur le meme site
      List<String> screenIdsWithDifferentSiteId = getScreenIdsWithDifferentSiteId(siteId,
        screenIds,
        fluxEvent);

      // tout les ecrans sont sur le meme site que le flux courant
      if (screenIdsWithDifferentSiteId.isEmpty()) {
        sendFluxEvent(fluxEvent, scheduleId);
      }
      else {
        List<String> screenIdsWithSameSiteId = getScreenIdsWithSameSiteId(screenIds, screenIdsWithDifferentSiteId);

        StringBuilder screenIdsWithSameSiteIdAsString = new StringBuilder();
        for (String s : screenIdsWithSameSiteId)
        {
          screenIdsWithSameSiteIdAsString.append(s);
        }
        if (!screenIdsWithSameSiteIdAsString.toString().isEmpty()) {
          // envoi du flux prevu
          sendFluxEvent(new FluxEvent(fluxEvent.getFluxId(), screenIdsWithSameSiteIdAsString.toString()), scheduleId);
        }

        StringBuilder screenIdsWithDifferentSiteIdAsString = new StringBuilder();
        for (String s : screenIdsWithDifferentSiteId)
        {
          screenIdsWithDifferentSiteIdAsString.append(s);
        }
        // recuperation d'un flux de fallback random
        Flux fallback = getRandomFallBackFlux(scheduleId);
        if (fallback != null) {
          sendFluxEvent(new FluxEvent(fallback.getId(), screenIdsWithDifferentSiteIdAsString.toString()), scheduleId);
        }

      }
    }
    // le flux courant est general
    else {
      sendFluxEvent(fluxEvent, scheduleId);
    }
  }