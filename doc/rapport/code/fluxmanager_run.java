public void run() {
    while (running) {
        // il y a des event a envoyer
        if (!fluxEvents.isEmpty()) {
            FluxEvent currentFlux = fluxEvents.remove(0);

            boolean run = true;

            do {
                eventController.send(
                    currentFlux.getFlux().getType().toLowerCase() +
                        "?" +
                        currentFlux.getFlux().getUrl() +
                        "|" +
                        String.join(",", currentFlux.getMacs())
                );

                if (!fluxEvents.isEmpty()) {
                    currentFlux = fluxEvents.remove(0);
                    if (fluxEvents.isEmpty()) {
                        run = false;
                    }
                }
                else {
                    run = false;
                }
            } while (run);
        }
        // attend un peu avant de recommencer
        else {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}