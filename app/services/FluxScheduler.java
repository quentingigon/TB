package services;

import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class FluxScheduler extends Observable implements Runnable {

	private FluxEvent currentFlux;
	private List<FluxEvent> fluxEvents;

	private boolean running;

	public FluxScheduler() {
		fluxEvents = new ArrayList<>();
		running = false;
	}

	public FluxScheduler(List<FluxEvent> fluxEvents) {
		this.fluxEvents = fluxEvents;
	}

	private void activate() {
		running = true;
	}
	private void deactivate() {
		running = false;
	}

	@Override
	public void run() {

		activate();

		while (running) {

			if (!fluxEvents.isEmpty()) {
				fluxEvents.add(fluxEvents.get(0));
				currentFlux = fluxEvents.remove(0);

				boolean bool = true;

				do {
					setChanged();
					notifyObservers(Source.single(currentFlux.getFlux().getUrl() + "|" + String.join(",", currentFlux.getMacs())));

					if (!fluxEvents.isEmpty()) {
						fluxEvents.add(fluxEvents.get(0));
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
		}
	}

	public void addFluxEvent(FluxEvent event) {
		fluxEvents.add(event);
	}
}
