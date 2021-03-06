package services;

import controllers.EventSourceController;
import models.FluxEvent;
import models.db.*;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.SimpleDateFormat;
import java.util.*;

import static controllers.CronUtils.checkIfScheduleAndDiffuserDaysOverlap;

/**
 * This class is charged with handling every Events generated by the Listeners.
 * It is here that the following verifications are made:
 *  - check if flux is located and on the correct site.
 *  - check if there is a Diffuser for the screens concerned by the event and replace the flux sent.
 */
@Singleton
public class EventManager {

	private final EventSourceController eventController;
	private final ServicePicker servicePicker;
	private final FluxChecker fluxChecker;


	@Inject
	public EventManager(EventSourceController eventSourceController,
						ServicePicker servicePicker,
						FluxChecker fluxChecker) {
		this.eventController = eventSourceController;
		this.servicePicker = servicePicker;
		this.fluxChecker = fluxChecker;
	}

	/**
	 * Entry point for the class. used by Listeners
	 * @param job job containing the event
	 * @param fluxHasNothingToDisplay true if flux must be replaced by a fallback
	 */
	public void handleEvent (EventJob job, boolean fluxHasNothingToDisplay) {
		FluxEvent event = job.getEvent();

		// if system detects that this flux has nothing to display,
		// replace it by one of the fallbacks
		if (fluxHasNothingToDisplay && job.isJobFromSchedule()) {
			Flux fallback = getRandomFallBackFlux(job.getEntityId());
			if (fallback != null) {
				event = new FluxEvent(fallback.getId(), event.getScreenIds());
			}
		}

		// there is an active schedule for the screens concerned by the job
		if (job.isJobFromSchedule()) {
			sendFluxEventAsGeneralOrLocated(event, job.getEntityId());
		}
		// job is from a diffuser
		else if (!fluxHasNothingToDisplay) {
			sendFluxEvent(event, job.getEntityId());
		}
	}

	/**
	 * send the event as a general or located one. for every screens on a different site than
	 * a located flux, replace it by a fallback flux
	 * @param fluxEvent the event so send
	 * @param scheduleId the id of the concerned schedule
	 */
	private void sendFluxEventAsGeneralOrLocated(FluxEvent fluxEvent, int scheduleId) {
		FluxService fluxService = servicePicker.getFluxService();
		Flux currentFlux = fluxService.getFluxById(fluxEvent.getFluxId());

		// current flux is a located one
		if (fluxService.getLocatedFluxByFluxId(currentFlux.getId()) != null) {
			int siteId = fluxService.getLocatedFluxByFluxId(currentFlux.getId()).getSiteId();
			List<String> screenIds = new ArrayList<>(Arrays.asList(fluxEvent.getScreenIds().split(",")));

			// get ids of screen with a different siteId than the fluxEvent
			List<String> screenIdsWithDifferentSiteId = getScreenIdsWithDifferentSiteId(siteId,
				screenIds,
				fluxEvent);

			// all screens are related to the same site as the flux
			if (screenIdsWithDifferentSiteId.isEmpty()) {
				// send event to observer
				sendFluxEvent(fluxEvent, scheduleId);
			}
			// some screens are not on the same sites
			else {

				List<String> screenIdsWithSameSiteId = getScreenIdsWithSameSiteId(screenIds, screenIdsWithDifferentSiteId);

				StringBuilder screenIdsWithSameSiteIdAsString = new StringBuilder();
				for (String s : screenIdsWithSameSiteId)
				{
					screenIdsWithSameSiteIdAsString.append(s);
				}
				if (!screenIdsWithSameSiteIdAsString.toString().isEmpty()) {
					// send planned flux
					sendFluxEvent(new FluxEvent(fluxEvent.getFluxId(), screenIdsWithSameSiteIdAsString.toString()), scheduleId);
				}

				StringBuilder screenIdsWithDifferentSiteIdAsString = new StringBuilder();
				for (String s : screenIdsWithDifferentSiteId)
				{
					screenIdsWithDifferentSiteIdAsString.append(s);
				}

				Flux fallback = getRandomFallBackFlux(scheduleId);
				if (fallback != null) {
					// send fallback flux to screens on different site
					sendFluxEvent(new FluxEvent(fallback.getId(), screenIdsWithDifferentSiteIdAsString.toString()), scheduleId);
				}
			}
		}
		// current flux is a general one
		else {
			sendFluxEvent(fluxEvent, scheduleId);
		}
	}

	/**
	 * get a random fallback flux from the Schedule
	 * @param scheduleId id of the Schedule
	 * @return a random flux
	 */
	private Flux getRandomFallBackFlux(int scheduleId) {
		FluxService fluxService = servicePicker.getFluxService();
		// bad random
		Random rand = new Random();
		List<Integer> fallbackIds = fluxService
			.getFallBackIdsOfScheduleById(scheduleId);

		// send a fallback flux if wrong location
		if (!fallbackIds.isEmpty()) {
			 return fluxService
				.getFluxById(fallbackIds
					.get(rand.nextInt(fallbackIds.size())));

		}
		else {
			return null;
		}
	}

	private List<String> getScreenIdsWithSameSiteId(List<String> screenIds, List<String> screenIdsWithDifferentSiteId) {
		Set<String> allScreens = new HashSet<>(screenIds);

		allScreens.removeAll(screenIdsWithDifferentSiteId);

		return new ArrayList<>(allScreens);
	}

	private List<String> getScreenIdsWithDifferentSiteId(int siteId, List<String> screenIds, FluxEvent fluxEvent) {
		// lists of screens to send the flux to
		List<String> screenIdsWithDifferentSiteId = new ArrayList<>();

		List<Screen> screens = getScreenList(fluxEvent.getScreenIds());

		for (Screen s : screens) {
			// if flux and screen are not restricted to the same site
			if (siteId != s.getSiteId()) {
				screenIdsWithDifferentSiteId.add(String.valueOf(s.getId()));
				screenIds.remove(String.valueOf(s.getId()));
			}
		}

		return screenIdsWithDifferentSiteId;
	}

	/**
	 * The function verifies if a RunningDiffuser is present for the screens concerned by the event,
	 * and if there is one and their start times overlap, send the diffused flux instead.
	 * @param event event to be checked
	 * @param scheduleId schedule concerned (can be null if event is from a Diffuser)
	 * @return a String containing the ids of the screens without a Diffuser
	 */
	private String sendDiffusedFluxToConcernedScreens(FluxEvent event, Integer scheduleId) {
		StringBuilder screenIdsWithNoDiffuser = new StringBuilder();
		List<Screen> screens = getScreenList(event.getScreenIds());
		DiffuserService diffuserService = servicePicker.getDiffuserService();

		for (Screen s: screens) {
			RunningDiffuser rd = getRunningDiffuserIfPresent(s);

			if (rd != null) {
				Diffuser diffuser = diffuserService.getDiffuserById(rd.getDiffuserId());

				// screens have an active schedule
				if (scheduleId != null) {
					Schedule schedule = servicePicker.getScheduleService().getScheduleById(scheduleId);

					if (schedule != null &&
						(checkIfScheduleAndDiffuserDaysOverlap(schedule, diffuser)
						&& checkIfScheduleAndDiffuserTimeOverlap(schedule, diffuser))) {

						StringBuilder screenIds = new StringBuilder();
						for (Integer id: diffuserService.getScreenIdsOfRunningDiffuserById(rd.getId())) {
							screenIds.append(id).append(",");
						}
						if (!screenIds.toString().isEmpty()) {
							screenIds.deleteCharAt(screenIds.length() - 1);
						}

						send(servicePicker.getFluxService().getFluxById(diffuser.getFlux()),
							getMacAddresses(screenIds.toString()));
					}
					else {
						screenIdsWithNoDiffuser.append(s.getId()).append(",");
					}
				}
				// no active schedule, just a diffuser
				else {
					send(servicePicker.getFluxService().getFluxById(diffuser.getFlux()),
						getMacAddresses(event.getScreenIds()));
				}
			}
			else {
				screenIdsWithNoDiffuser.append(s.getId()).append(",");
			}
		}
		// remove last ,
		if (!screenIdsWithNoDiffuser.toString().isEmpty()) {
			screenIdsWithNoDiffuser.deleteCharAt(screenIdsWithNoDiffuser.length() - 1);
		}

		return screenIdsWithNoDiffuser.toString();
	}

	private boolean checkIfScheduleAndDiffuserTimeOverlap(Schedule schedule, Diffuser diffuser) {
		List<FluxTrigger> triggers = servicePicker.getFluxService().getFluxTriggersOfScheduleById(schedule.getId());
		triggers.sort(Comparator.comparing(FluxTrigger::getTime));

		List<FluxLoop> loops = servicePicker.getFluxService().getFluxLoopOfScheduleById(schedule.getId());

		Flux diffusedFlux = servicePicker.getFluxService().getFluxById(diffuser.getFlux());

		DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
		LocalTime diffuserStartTime = formatter.parseLocalTime(diffuser.getTime());
		LocalTime diffuserEndTime = diffuserStartTime.plusMinutes(diffusedFlux.getTotalDuration());

		String currentTime = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
		String startTime = formatter.print(diffuserStartTime);
		String endTime = formatter.print(diffuserEndTime);

		for (FluxTrigger ft: triggers) {
			String fluxTriggerTime = ft.getTime();

			// if time of trigger is not after current time
			if (fluxTriggerTime.compareTo(currentTime) >= 0) {
				if (isTimeBetweenBounds(fluxTriggerTime, startTime, endTime)) {
					return true;
				}
				break; // break to go out of the for loop. we must indeed only check the next FluxTrigger
			}
		}

		// we are in a loop, so we must just check if current time is overlapping with diffuser time
		if (!loops.isEmpty()) {
			if (isTimeBetweenBounds(currentTime, startTime, endTime)) {
				return true;
			}
		}
		return false;
	}

	private boolean isTimeBetweenBounds(String time, String lowerBound, String upperBound) {
		return time.compareTo(lowerBound) == 0 ||
			//time.compareTo(upperBound) == 0 ||
			(time.compareTo(lowerBound) > 0 && time.compareTo(upperBound) < 0);
	}

	/**
	 * makes the verification of the presence or not of Diffuser for the screens concerned
	 * and then calls the send() method
	 * @param event event to verify and send
	 * @param scheduleId schedule concerned (can be null if event is from a Diffuser)
	 */
	private void sendFluxEvent(FluxEvent event, Integer scheduleId) {
		Flux currentFlux = getFlux(event.getFluxId());

		if (currentFlux != null) {

			// send diffused flux to correct screen and returns the ids of the other screens
			String screenIdsWithNoActiveDiffuser = sendDiffusedFluxToConcernedScreens(event, scheduleId);

			if (!screenIdsWithNoActiveDiffuser.isEmpty()) {
				List<String> macAddresses = getMacAddresses(screenIdsWithNoActiveDiffuser);

				send(currentFlux, macAddresses);
			}
		}
	}

	/**
	 * This function is the output way of this class, as it sends an event to the EventSourceController
	 * @param currentFlux flux to send
	 * @param macAddresses mac addresses of the screens concerned
	 */
	private void send(Flux currentFlux, List<String> macAddresses) {
		System.out.println("Sending event : " + currentFlux.getType().toLowerCase() + "?" + currentFlux.getUrl() + "|" + String.join(",", macAddresses));
		eventController.send(
			currentFlux.getType().toLowerCase() +
				"?" +
				currentFlux.getUrl() +
				"|" +
				String.join(",", macAddresses)
		);
	}

	private RunningDiffuser getRunningDiffuserIfPresent(Screen s) {
		DiffuserService diffuserService = servicePicker.getDiffuserService();

		Integer runningDiffuserId = diffuserService.getRunningDiffuserIdByScreenId(s.getId());

		if (runningDiffuserId != null) {
			return diffuserService.getRunningDiffuserById(runningDiffuserId);
		}
		else {
			return null;
		}
	}

	private List<String> getMacAddresses(String screenIds) {
		List<String> macs = new ArrayList<>();

		for(Screen s: getScreenList(screenIds)) {
			macs.add(s.getMacAddress());
		}
		return macs;
	}

	private List<Screen> getScreenList(String screenIds) {
		ScreenService screenService = servicePicker.getScreenService();
		List<Screen> output = new ArrayList<>();
		String[] list = screenIds.split("\\s*,\\s*");
		for (String id: list) {
			if (screenService.getScreenById(Integer.valueOf(id)) != null) {
				output.add(screenService.getScreenById(Integer.valueOf(id)));
			}
		}
		return output;
	}

	private Flux getFlux(int id) {
		FluxService fluxService = servicePicker.getFluxService();

		if (fluxService.getFluxById(id) != null) {
			return fluxService.getFluxById(id);
		}
		else {
			return null;
		}
	}

	public FluxChecker getFluxChecker() {
		return fluxChecker;
	}
}
