package services;

import akka.stream.javadsl.Source;

import javax.inject.Singleton;
import java.util.*;

@Singleton
public class FluxManager extends Observable implements Runnable, Observer {

	private FluxEvent currentFlux;
	private List<FluxEvent> fluxEvents;
	private Source<String, ?> source;

	private boolean running;

	private static final FluxManager instance = new FluxManager();

	private FluxManager() {
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

	public static final FluxManager getInstance()
	{
		return instance;
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
				currentFlux = fluxEvents.remove(0);

				boolean bool = true;

				// notify observer with url + macs
				do {
					setChanged();
					//source = Source.single(currentFlux.getFlux().getUrl() + "|" + String.join(",", currentFlux.getMacs()));

					notifyObservers(currentFlux.getFlux().getUrl() + "|" + String.join(",", currentFlux.getMacs()));

					System.out.println("Source updated " + currentFlux.getFlux());

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
				System.out.println("No flux events");
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public Source<String, ?> getSource() {
		return source;
	}
}
