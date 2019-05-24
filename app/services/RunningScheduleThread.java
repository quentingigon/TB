package services;

import models.FluxEvent;
import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Screen;
import models.repositories.interfaces.FluxRepository;
import org.joda.time.DateTime;

import java.util.*;

import static services.BlockUtils.*;
import static services.RunningScheduleUtils.SITE_ERROR;
import static services.RunningScheduleUtils.WAIT_FLUX;

public class RunningScheduleThread extends Observable implements Runnable {

	// /!\ WARNING /!\ maybe it's a "static" instance of FluxRepository and is not updated afterwards
	private FluxRepository fluxRepository;
	private FluxChecker fluxChecker;

	private RunningSchedule runningSchedule;
	private List<Screen> screens;

	private volatile HashMap<Integer, Integer> timetable;
	private HashMap<Integer, List<Integer>> timetableHistory;
	private List<Integer> fallbackFluxIds;

	private boolean running;

	private volatile FluxEvent lastFluxEvent;

	public RunningScheduleThread(RunningSchedule runningSchedule,
								 List<Screen> screens,
								 List<Integer> fallbackFluxIds,
								 Map<Integer, Integer> timetable,
								 FluxRepository fluxRepository,
								 FluxChecker fluxChecker) {
		this.fluxChecker = fluxChecker;
		this.runningSchedule = runningSchedule;
		this.screens = screens;
		this.timetable = (HashMap<Integer, Integer>) timetable;
		this.fallbackFluxIds = fallbackFluxIds;
		this.fluxRepository = fluxRepository;
		this.timetableHistory = new HashMap<>();
		running = true;
		lastFluxEvent = null;
	}

	@Override
	public void run() {

		while (running) {

			DateTime dt = new DateTime();
			int hours = dt.getHourOfDay();
			int minutes = dt.getMinuteOfHour();

			if (hours >= beginningHour && hours < endHour) {
				int blockIndex = getBlockNumberOfTime(hours, minutes);
				Flux lastFluxSent = null;

				do {
					Flux currentFlux = fluxRepository.getById(timetable.get(blockIndex++));

					boolean doNotSend = false;
					// if the flux is of type video and was already sent one time
					// -> do not resend the event
					if (lastFluxSent != null &&
						currentFlux != null &&
						currentFlux.getName().equals(lastFluxSent.getName()) &&
						currentFlux.getType().equals("VIDEO")) {
						doNotSend = true;
					}

					Flux nextFlux = fluxRepository.getById(timetable.get(blockIndex));

					// if next flux has data that must be checked before displaying
					if (nextFlux.getDataCheckUrl() != null) {
						// if next flux has no data to display
						if (!fluxChecker.checkIfFluxHasSomethingToDisplayByDateTime(nextFlux)) {
							// remove it from the timetable to make room for other fluxes
							removeScheduledFlux(nextFlux);
						}
					}

					if (!doNotSend) {

						// if a flux is scheduled for that block
						if (currentFlux != null) {
							sendFluxEventAsGeneralOrLocated(currentFlux);
							lastFluxSent = currentFlux;
						}
						// choose from the unscheduled fluxes
						else {
							int freeBlocksN = getNumberOfBlocksToNextScheduledFlux(blockIndex);

							boolean sent = false;

							Collections.shuffle(fallbackFluxIds);

							for (Integer fluxId : fallbackFluxIds) {

								if (!sent) {
									Flux flux = fluxRepository.getById(fluxId);
									// if this flux can be inserted in the remaining blocks
									if (flux != null && flux.getTotalDuration() <= freeBlocksN) {

										// update timetable
										scheduleFlux(flux, blockIndex);

										// send event to observer
										sendFluxEventAsGeneralOrLocated(flux);
										lastFluxSent = currentFlux;
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
					}


				} while (blockIndex < blockNumber && running);
			}
		}
	}

	// This function send the flux to the correct screens, so as a general flux or a located one
	private void sendFluxEventAsGeneralOrLocated(Flux flux) {
		// current flux is a located one
		if (fluxRepository.getLocatedFluxByFluxId(flux.getId()) != null) {
			int siteId = fluxRepository.getLocatedFluxByFluxId(flux.getId()).getSiteId();

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
				sendFluxEvent(flux, screens);
			}
			else {
				sendFluxEvent(flux, screensWithSameSiteId);
				sendFluxEvent(fluxRepository.getByName(SITE_ERROR), screensWithDifferentSiteId);
			}
		}
		// current flux is a general one
		else {
			sendFluxEvent(flux, screens);
		}
	}

	private void sendFluxEvent(Flux flux, List<Screen> screenList) {
		FluxEvent event = new FluxEvent(flux, screenList);
		lastFluxEvent = event;
		setChanged();
		notifyObservers(event);
	}

	public void resendLastFluxEvent() {
		if (lastFluxEvent != null) {
			setChanged();
			notifyObservers(lastFluxEvent);
		}
	}

	public void resendLastFluxEventToScreens(List<Screen> screenList) {
		if (lastFluxEvent != null) {
			setChanged();
			notifyObservers(new FluxEvent(lastFluxEvent.getFlux(), screenList));
		}
		else {
			setChanged();
			notifyObservers(new FluxEvent(fluxRepository.getByName(WAIT_FLUX), screenList));
		}
	}

	public void sendFluxToScreensImmediately(Flux flux, List<Screen> screenList) {
		sendFluxEvent(flux, screenList);
	}

	private void scheduleFlux(Flux flux, int blockIndex) {
		for (int i = 0; i < flux.getTotalDuration(); i++) {
			// add the flux to all the block from blockIndex to blockIndex + flux duration
			this.timetable.put(blockIndex + i, flux.getId());
		}
	}

	public void scheduleFluxFromDiffuser(Flux flux, int blockIndex, int diffuserId) {
		timetableHistory.computeIfAbsent(diffuserId, k -> new ArrayList<>());
		for (int i = 0; i < flux.getTotalDuration(); i++) {
			// save old timetable
			timetableHistory.get(diffuserId).add(timetable.get(blockIndex + i));
			// add the flux to all the block from blockIndex to blockIndex + flux duration
			this.timetable.put(blockIndex + i, flux.getId());
		}
	}

	public void scheduleFluxIfPossible(Flux flux, int blockIndex) {
		// if we can schedule the flux in the timetable (enough space until next scheduled flux)
		if (!timetable.get(blockIndex).equals(-1) && getNumberOfBlocksToNextScheduledFlux(blockIndex) >= flux.getTotalDuration()) {
			scheduleFlux(flux, blockIndex);
		}
	}

	public void scheduleFluxIfPossibleFromDiffuser(Flux flux, int blockIndex, int diffuserId) {
		if (!timetable.get(blockIndex).equals(-1) && getNumberOfBlocksToNextScheduledFlux(blockIndex) >= flux.getTotalDuration()) {
			scheduleFluxFromDiffuser(flux, blockIndex, diffuserId);
		}
	}

	public void removeScheduledFluxFromDiffuser(Flux flux, int diffuserId, int blockIndex) {
		int index = 0;
		for (int i = blockIndex; i < flux.getTotalDuration(); i++) {
			// re-put the flux value before the diffuser was activated
			this.timetable.put(i, timetableHistory.get(diffuserId).get(index++));
		}
		timetableHistory.remove(diffuserId);
	}

	public void removeScheduledFlux(Flux flux) {
		int index = new ArrayList<>(timetable.keySet()).indexOf(flux.getId());
		for (int i = index; i < flux.getTotalDuration(); i++) {
			if (this.timetable.get(i).equals(flux.getId())) {
				this.timetable.put(i, -1);
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

	public RunningSchedule getRunningSchedule() {
		return runningSchedule;
	}

	public void setRunningSchedule(RunningSchedule runningSchedule) {
		this.runningSchedule = runningSchedule;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
