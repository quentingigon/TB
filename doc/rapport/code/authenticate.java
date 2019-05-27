public Result authentification(Http.Request request) {
        
        String macAdr = request.queryString().get("mac")[0];
        Screen screen = getScreenByMacAddress(macAdr);

        // Ecran inconnu du systeme
        if (screen == null) {

            // L'ecran a deja essayer de s'identifier
            if (getWaitingScreenByMacAddress(macAdr) != null) {
                return ok(screen_code.render(getWaitingScreenByMacAddress(macAdr).getCode()));
            }

            String code = screenRegisterCodeGenerator();
            add(new WaitingScreen(code, macAdr));

            // Envoi du code
            return ok(screen_code.render(code));
        }
        // Ecran connu du systeme
        else {
            // Pas de Schedule actif pour cet ecran
            if (getRunningScheduleIdByScreenId(screen.getId()) == null) {
                return redirect(routes.ErrorPageController.noScheduleView());
            }

            // Ecran actif
            if (!screen.isLogged()) {
                screen.setLogged(true);
            }
            return ok(eventsource.render()).withCookies(
                Http.Cookie.builder("mac", macAdr)
                    .withHttpOnly(false)
                    .build(),
                Http.Cookie.builder("resolution", screen.getResolution())
                    .withHttpOnly(false)
                    .build());
        }
    }