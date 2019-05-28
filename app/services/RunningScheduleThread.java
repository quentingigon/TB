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

	private FluxRepository fluxRepository;
	private FluxChecker fluxChecker;

	private RunningSchedule runningSchedule;
	private volatile List<Screen> screens;

	private volatile HashMap<Integer, Integer> timetable;
	private volatile HashMap<Integer, List<Integer>> timetableHistory;
	private List<Integer> unscheduledFluxIds;

	private volatile boolean running;
	private boolean keepOrder;

	private volatile FluxEvent lastFluxEvent;

	public RunningScheduleThread(RunningSchedule runningSchedule,
								 List<Screen> screens,
								 List<Integer> unscheduledFluxIds,
								 Map<Integer, Integer> timetable,
								 FluxRepository fluxRepository,
								 FluxChecker fluxChecker,
								 boolean keepOrder) {
		this.fluxChecker = fluxChecker;
		this.runningSchedule = runningSchedule;
		this.screens = screens;
		this.timetable = (HashMap<Integer, Integer>) timetable;
		this.unscheduledFluxIds = unscheduledFluxIds;
		this.fluxRepository = fluxRepository;
		this.timetableHistory = new HashMap<>();
		running = true;
		this.keepOrder = keepOrder;
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

					// TODO finish flux checking, there are some errors
					// if next flux has data that must be checked before displaying
					if (nextFlux != null && nextFlux.getDataCheckUrl() != null) {
						// if next flux has no data to display

						/*
						if (!fluxChecker.checkIfFluxHasSomethingToDisplayByDateTime(nextFlux)) {
							// remove it from the timetable to make room for other fluxes
							removeScheduledFlux(nextFlux);
						}
						 */
					}

					if (!doNotSend) {

						// if a flux is scheduled for that block
						if (currentFlux != null) {
							sendFluxEventAsGeneralOrLocated(currentFlux);
							lastFluxSent = currentFlux;
						}
						// choose from the unscheduled fluxes
						else if (!unscheduledFluxIds.isEmpty()) {
							int freeBlocksN = getNumberOfBlocksToNextScheduledFlux(blockIndex);

							boolean sent = false;
							boolean noPossibleFlux = false;
							int tryouts = 0;
							int fluxId;

							do {
								// If it was specified at the schedule creation that the unscheduled fluxes
								// must be sent in hte
								if (keepOrder) {
									// make a cycle
									unscheduledFluxIds.add(unscheduledFluxIds.get(0));
									fluxId = unscheduledFluxIds.remove(0);
								}
								else {
									Collections.shuffle(unscheduledFluxIds);
									fluxId = unscheduledFluxIds.get(0);
								}

								Flux unscheduledFlux = fluxRepository.getById(fluxId);

								// if this flux can be inserted in the remaining blocks
								if (unscheduledFlux != null && unscheduledFlux.getTotalDuration() <= freeBlocksN) {

									// update timetable
									scheduleFlux(unscheduledFlux, blockIndex);

									// send event to observer
									sendFluxEventAsGeneralOrLocated(unscheduledFlux);
									lastFluxSent = unscheduledFlux;
									sent = true;
								}
								else {
									// if we looped through all possibilities or at least
									if (++tryouts == unscheduledFluxIds.size()) {
										noPossibleFlux = true;
									}
								}
							} while (!sent && !noPossibleFlux);

							// no flux was sent, resend last flux if not null
							if (noPossibleFlux && lastFluxSent != null) {
								sendFluxEventAsGeneralOrLocated(lastFluxSent);
							}
						}
						// TODO add else if for fallbacks
						// send error flux if no flux
						else {
							sendFluxEvent(fluxRepository.getByName(WAIT_FLUX), screens);
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
		for (int i = blockIndex; i < flux.getTotalDuration(); i++) {
			// add the flux to all the block from blockIndex to blockIndex + flux duration
			this.timetable.put(i, flux.getId());
		}
	}

	public void scheduleFluxFromDiffuser(Flux flux, int blockIndex, int diffuserId) {
		this.timetableHistory.computeIfAbsent(diffuserId, k -> new ArrayList<>());
		for (int i = 0; i < flux.getTotalDuration(); i++) {
			// save old timetable
			this.timetableHistory.get(diffuserId).add(timetable.get(blockIndex + i));
			// add the flux to all the block from blockIndex to blockIndex + flux duration
			this.timetable.put(blockIndex + i, flux.getId());
		}
	}

	public void scheduleFluxIfPossible(Flux flux, int blockIndex) {
		// if we can schedule the flux in the timetable (enough space until next scheduled flux)
		if (!this.timetable.get(blockIndex).equals(-1) && getNumberOfBlocksToNextScheduledFlux(blockIndex) >= flux.getTotalDuration()) {
			scheduleFlux(flux, blockIndex);
		}
	}

	public void scheduleFluxIfPossibleFromDiffuser(Flux flux, int blockIndex, int diffuserId) {
		if (this.timetable.get(blockIndex).equals(-1) && getNumberOfBlocksToNextScheduledFlux(blockIndex) >= flux.getTotalDuration()) {
			scheduleFluxFromDiffuser(flux, blockIndex, diffuserId);
		}
	}

	public void removeScheduledFluxFromDiffuser(Flux flux, int diffuserId, int blockIndex) {
		int index = 0;
		for (int i = 0; i < flux.getTotalDuration(); i++) {
			// re-put the flux value before the diffuser was activated
			this.timetable.put(blockIndex + i, this.timetableHistory.get(diffuserId).get(index++));
		}
		this.timetableHistory.remove(diffuserId);
	}

	public void removeScheduledFlux(Flux flux) {
		int index = new ArrayList<>(this.timetable.keySet()).indexOf(flux.getId());
		for (int i = index; i < flux.getTotalDuration(); i++) {
			if (this.timetable.get(i).equals(flux.getId())) {
				this.timetable.put(i, -1);
			}
		}
	}

	private int getNumberOfBlocksToNextScheduledFlux(int blockIndex) {
		List<Integer> fluxes = new ArrayList<>(this.timetable.values());
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

	public void abort() {
		this.running = false;
	}

	public HashMap<Integer, Integer> getTimetable() {
		return timetable;
	}

	public synchronized void removeFromScreens(Screen screen) {
		this.screens.remove(screen);
	}
}
