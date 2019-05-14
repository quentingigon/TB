package services;

import controllers.EventSourceControllerS;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

@Singleton
public class FluxManager extends Observable implements Runnable, Observer {

	private final EventSourceControllerS eventController;

	private List<FluxEvent> fluxEvents;

	private boolean running;

	@Inject
	private FluxManager(EventSourceControllerS eventController) {
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

				// notify observer with url + macs
				do {
					// setChanged();
					//source = Source.single(currentFlux.getFlux().getUrl() + "|" + String.join(",", currentFlux.getMacs()));
					System.out.println("Sending event : " + currentFlux.getFlux().getType().toLowerCase() + "?" + currentFlux.getFlux().getUrl() + "|" + String.join(",", currentFlux.getMacs()));
					//notifyObservers(currentFlux.getFlux().getUrl() + "|" + String.join(",", currentFlux.getMacs()));

					eventController.send(
						currentFlux.getFlux().getType().toLowerCase() +
							"?" +
							currentFlux.getFlux().getUrl() +
							"|" +
							String.join(",", currentFlux.getMacs())
					);

					System.out.println("Source updated " + currentFlux.getFlux());

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
