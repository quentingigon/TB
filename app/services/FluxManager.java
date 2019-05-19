package services;

import controllers.EventSourceControllerS;
import models.db.Screen;
import models.repositories.ScreenRepository;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@Singleton
public class FluxManager extends Observable implements Runnable, Observer {

	private final EventSourceControllerS eventController;
	private final ScreenRepository screenRepository;

	private List<FluxEvent> fluxEvents;

	private boolean running;

	@Inject
	private FluxManager(EventSourceControllerS eventController,
						ScreenRepository screenRepository) {
		this.screenRepository = screenRepository;
		this.eventController = eventController;
		fluxEvents = new ArrayList<>();
		running = false;
	}

	private void activate() {
		running = true;
	}
	private void deactivate() {
		running = false;
	}

	@Override
	public synchronized void update(Observable o, Object arg) {
		if (arg instanceof FluxEvent) {
			fluxEvents.add((FluxEvent) arg);
			System.out.println("Flux " + ((FluxEvent) arg).getFlux().getName() + " was added to manager");
		}
	}

	@Override
	public void run() {

		activate();

		while (running) {

			// there are flux events to send
			if (!fluxEvents.isEmpty()) {
				FluxEvent currentFlux = fluxEvents.remove(0);

				boolean bool = true;

				do {
					System.out.println("Sending event : " + currentFlux.getFlux().getType().toLowerCase() + "?" + currentFlux.getFlux().getUrl() + "|" + String.join(",", currentFlux.getMacs()));
					eventController.send(
						currentFlux.getFlux().getType().toLowerCase() +
							"?" +
							currentFlux.getFlux().getUrl() +
							"|" +
							String.join(",", currentFlux.getMacs())
					);

					// updating concerned screens
					for (String screenMac: currentFlux.getMacs()) {
						Screen screen = screenRepository.getByMacAddress(screenMac);
						screen.setCurrentFluxName(currentFlux.getFlux().getName());
						screenRepository.update(screen);
					}

					if (!fluxEvents.isEmpty()) {
						// fluxEvents.addFlux(fluxEvents.get(0));
						currentFlux = fluxEvents.remove(0);

						if (fluxEvents.isEmpty()) {
							bool = false;
						}
					}
					else {
						bool = false;
					}
				} while (bool);
			}
			// wait a bit before rechecking
			else {
				// System.out.println("No flux events");
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
