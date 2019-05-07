package services;

import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Screen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import static services.BlockUtils.blockDuration;
import static services.BlockUtils.blockNumber;

public class RunningScheduleService extends Observable implements Runnable {

	private RunningSchedule runningSchedule;
	private List<Flux> fluxes;
	private List<Screen> screens;

	private HashMap<Integer, Flux> timetable;
	private List<Flux> fallbackFluxes;

	private boolean running;

	public RunningScheduleService(RunningSchedule runningSchedule, List<Flux> fluxes, List<Screen> screens) {
		this.runningSchedule = runningSchedule;
		this.fluxes = fluxes;
		this.screens = screens;
		running = true;
	}

	public RunningScheduleService(RunningSchedule runningSchedule,
								  List<Screen> screens,
								  List<Flux> fallbackFluxes,
								  HashMap<Integer, Flux> timetable) {
		this.runningSchedule = runningSchedule;
		this.screens = screens;
		this.timetable = timetable;
		this.fallbackFluxes = fallbackFluxes;
		running = true;
	}

	@Override
	public void run() {

		while (running) {

			int blockIndex = 0;
			Flux lastFlux = new Flux();

			do {
				Flux currentFlux = timetable.get(blockIndex++);

				// if a flux is scheduled for that block
				if (currentFlux != null) {

					// send an update only if flux has changed
					// TODO need to check for fluxes with multiple phases
					if (lastFlux != currentFlux) {

						// send event to observer
						sendFluxEvent(currentFlux);

						lastFlux = currentFlux;
					}
				}
				// choose from the unscheduled fluxes
				else {
					int freeBlocksN = getNumberOfBlocksToNextScheduledFlux(blockIndex);

					// TODO optimize
					for (Flux flux : fallbackFluxes) {

						// send an update only if flux has changed
						// TODO need to check for fluxes with multiple phases
						if (flux != lastFlux) {

							// if this flux can be inserted in the remaining blocks
							if (flux.getDuration() <= freeBlocksN) {

								// update timetable
								scheduleFlux(flux, blockIndex);

								// send event to observer
								sendFluxEvent(flux);

								lastFlux = flux;
								break;
							}
						}
					}
				}

				try {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException("Thread interrupted");
					}
					Thread.sleep(blockDuration);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (blockIndex < blockNumber && running);

			/*
			if (!fluxes.isEmpty()) {

				// make rotation
				fluxes.add(fluxes.get(0));
				Flux flux = fluxes.remove(0);

				FluxEvent event = new FluxEvent(flux, screens);

				setChanged();
				notifyObservers(event);

				try {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException("Thread interrupted");
					}
					Thread.sleep(flux.getDuration());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			 */
		}
	}

	private void sendFluxEvent(Flux flux) {
		FluxEvent event = new FluxEvent(flux, screens);

		setChanged();
		notifyObservers(event);
	}

	private void scheduleFlux(Flux flux, int blockIndex) {
		for (int i = 0; i < flux.getDuration(); i++) {
			// add the flux to all the block from blockIndex to blockIndex + flux duration
			timetable.put(blockIndex + i, flux);
		}
	}

	private int getNumberOfBlocksToNextScheduledFlux(int blockIndex) {
		List<Flux> fluxes = new ArrayList<>(timetable.values());
		int n = 0;

		// max 900 iterations and that case will never happen, so it should
		// be fast enough
		for (Flux flux: fluxes.subList(blockIndex, fluxes.size())) {
			n++;
			if (flux != null) {
				break;
			}
		}
		return n;
	}

	public synchronized void removeFluxFromRunningSchedule(Flux flux) {
		fluxes.remove(flux);
	}

	public synchronized void addFluxToRunningSchedule(Flux flux) {
		fluxes.add(flux);
	}

	public RunningSchedule getRunningSchedule() {
		return runningSchedule;
	}

	public void setRunningSchedule(RunningSchedule runningSchedule) {
		this.runningSchedule = runningSchedule;
	}

	public List<Flux> getFluxes() {
		return fluxes;
	}

	public void setFluxes(List<Flux> fluxes) {
		this.fluxes = fluxes;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
