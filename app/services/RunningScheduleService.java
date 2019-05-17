package services;

import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Screen;
import models.repositories.FluxRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import static services.BlockUtils.blockDuration;
import static services.BlockUtils.blockNumber;

public class RunningScheduleService extends Observable implements Runnable {

	// TODO WARNING !!!! maybe it's a "static" instance of FluxRepository and is not updated afterwards
	private FluxRepository fluxRepository;

	private RunningSchedule runningSchedule;
	private List<Flux> fluxes;
	private List<Screen> screens;

	private volatile HashMap<Integer, Integer> timetable;
	private List<Integer> fallbackFluxIds;

	private boolean running;

	public RunningScheduleService(RunningSchedule runningSchedule,
								  List<Screen> screens,
								  List<Integer> fallbackFluxIds,
								  HashMap<Integer, Integer> timetable,
								  FluxRepository fluxRepository) {
		this.runningSchedule = runningSchedule;
		this.screens = screens;
		this.timetable = timetable;
		this.fallbackFluxIds = fallbackFluxIds;
		this.fluxRepository = fluxRepository;
		running = true;
	}

	@Override
	public void run() {

		while (running) {

			int blockIndex = 0;
			Flux lastFlux = new Flux();
			int siteId = 0;

			do {
				Flux currentFlux = fluxRepository.getById(timetable.get(blockIndex++));

				// if a flux is scheduled for that block
				if (currentFlux != null) {

					// current flux is a located one
					if (fluxRepository.getLocatedFluxByFluxId(currentFlux.getId()) != null) {
						siteId = fluxRepository.getLocatedFluxByFluxId(currentFlux.getId()).getSiteId();

						// lists of screens to send the flux
						List<Screen> screensWithSameSiteId = screens;
						List<Screen> screensWithDifferentSiteId = new ArrayList<>();

						for (Screen s : screens) {
							// if flux and screen are not restricted to the same site
							if (siteId != s.getSiteId()) {
								screensWithDifferentSiteId.add(s);
								screensWithSameSiteId.remove(s);
							}
						}

						// all screens are related to the same site as the flux
						if (screensWithDifferentSiteId.isEmpty()) {
							// send event to observer
							sendFluxEvent(currentFlux, screens);
						}
						else {
							sendFluxEvent(currentFlux, screensWithSameSiteId);
							// TODO send backup or error flux
							// sendFluxEvent(currentFlux, screensWithDifferentSiteId);
						}
					}
					// current flux is a general one
					else {
						siteId = -1;

						sendFluxEvent(currentFlux, screens);
					}
					lastFlux = currentFlux;
				}
				// choose from the unscheduled fluxes
				else {
					int freeBlocksN = getNumberOfBlocksToNextScheduledFlux(blockIndex);

					boolean sent = false;

					// TODO optimize
					for (Integer fluxId : fallbackFluxIds) {

						Flux flux = fluxRepository.getById(fluxId);

						if (!sent) {
							// if this flux can be inserted in the remaining blocks
							if (flux.getDuration() <= freeBlocksN) {

								// update timetable
								scheduleFlux(flux, blockIndex);

								// TODO fallback are general or located ?
								// send event to observer
								sendFluxEvent(flux, screens);

								lastFlux = flux;
								sent = true;
							}
						}
					}
				}

				try {
					if (Thread.currentThread().isInterrupted()) {
						throw new InterruptedException("Thread interrupted");
					}
					Thread.sleep((long) blockDuration * 60000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (blockIndex < blockNumber && running);
		}
	}

	private void sendFluxEvent(Flux flux, List<Screen> screenList) {
		FluxEvent event = new FluxEvent(flux, screenList);
		setChanged();
		notifyObservers(event);
	}

	public void scheduleFlux(Flux flux, int blockIndex) {
		for (int i = 0; i < flux.getDuration(); i++) {
			// add the flux to all the block from blockIndex to blockIndex + flux duration
			this.timetable.put(blockIndex + i, flux.getId());
		}
	}

	// TODO maybe optimize
	public void removeScheduledFlux(Flux flux) {
		for (int i = 0; i < flux.getDuration(); i++) {
			if (this.timetable.get(i).equals(flux.getId())) {
				this.timetable.remove(i);
			}
		}
	}

	private int getNumberOfBlocksToNextScheduledFlux(int blockIndex) {
		List<Integer> fluxes = new ArrayList<>(timetable.values());
		int n = 0;

		// max 900 iterations and that case will never happen, so it should
		// be fast enough
		for (Integer fluxId: fluxes.subList(blockIndex, fluxes.size() - 1)) {
			if (fluxId != -1) {
				break;
			}
			n++;
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
