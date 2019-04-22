package services;

import akka.stream.javadsl.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class FluxManager extends Observable implements Runnable, Observer {

	private FluxEvent currentFlux;
	private List<FluxEvent> fluxEvents;

	private boolean running;

	public FluxManager() {
		fluxEvents = new ArrayList<>();
		running = false;
	}

	public FluxManager(List<FluxEvent> fluxEvents) {
		this.fluxEvents = fluxEvents;
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
		}
	}

	@Override
	public void run() {

		activate();

		while (running) {

			// there are flux events to send
			if (!fluxEvents.isEmpty()) {
				// fluxEvents.add(fluxEvents.get(0));
				currentFlux = fluxEvents.remove(0);

				boolean bool = true;

				// notify observer with url + macs
				do {
					setChanged();
					notifyObservers(Source.single(currentFlux.getFlux().getUrl() + "|" + String.join(",", currentFlux.getMacs())));

					if (!fluxEvents.isEmpty()) {
						// fluxEvents.add(fluxEvents.get(0));
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
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
