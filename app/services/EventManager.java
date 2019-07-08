package services;

import controllers.EventSourceController;
import models.FluxEvent;
import models.db.*;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

import static controllers.CronUtils.checkIfScheduleAndDiffuserDaysOverlap;


@Singleton
public class EventManager {

	private final EventSourceController eventController;
	private final ServicePicker servicePicker;
	private final FluxChecker fluxChecker;

	private FluxEvent lastEvent;

	@Inject
	public EventManager(EventSourceController eventSourceController,
						ServicePicker servicePicker,
						FluxChecker fluxChecker) {
		this.eventController = eventSourceController;
		this.servicePicker = servicePicker;
		this.fluxChecker = fluxChecker;
		lastEvent = null;
	}

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
					sendFluxEvent(new FluxEvent(fallback.getId(), screenIdsWithDifferentSiteIdAsString.toString()), scheduleId);
				}

			}
		}
		// current flux is a general one
		else {
			sendFluxEvent(fluxEvent, scheduleId);
		}
	}

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
						send(servicePicker.getFluxService().getFluxById(diffuser.getFlux()),
							getMacAddresses(event.getScreenIds()));
					}
				}
				// no active schedule, just a diffuser
				else {
					send(servicePicker.getFluxService().getFluxById(diffuser.getFlux()),
						getMacAddresses(event.getScreenIds()));
				}
			}
			else {
				screenIdsWithNoDiffuser.append(s.getId());
			}
		}

		return screenIdsWithNoDiffuser.toString();
	}

	private boolean checkIfScheduleAndDiffuserTimeOverlap(Schedule schedule, Diffuser diffuser) {
		List<FluxTrigger> triggers = servicePicker.getFluxService().getFluxTriggersOfScheduleById(schedule.getId());
		Flux diffusedFlux = servicePicker.getFluxService().getFluxById(diffuser.getFlux());

		DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
		LocalTime diffuserStartTime = formatter.parseLocalTime(diffuser.getTime());
		LocalTime diffuserEndTime = diffuserStartTime.plusMinutes(diffusedFlux.getTotalDuration());

		String startTime = formatter.print(diffuserStartTime);
		String endTime = formatter.print(diffuserEndTime);

		for (FluxTrigger ft: triggers) {

			String fluxTriggerTime = ft.getTime();

			if (fluxTriggerTime.compareTo(startTime) == 0 ||
				fluxTriggerTime.compareTo(endTime) == 0 ||
				(fluxTriggerTime.compareTo(startTime) > 0 && fluxTriggerTime.compareTo(endTime) < 0)) {
				return true;
			}
		}
		return false;
	}

	private void sendFluxEvent(FluxEvent event, Integer scheduleId) {
		Flux currentFlux = getFlux(event.getFluxId());

		if (currentFlux != null) {

			// send diffused flux to correct screen and returns the ids of the other screens
			String screenIdsWithNoActiveDiffuser = sendDiffusedFluxToConcernedScreens(event, scheduleId);

			if (!screenIdsWithNoActiveDiffuser.isEmpty()) {
				List<String> macAddresses = getMacAddresses(screenIdsWithNoActiveDiffuser);

				send(currentFlux, macAddresses);
				lastEvent = event;
			}
		}
	}

	private void send(Flux currentFlux, List<String> macAddresses) {
		ScreenService screenService = servicePicker.getScreenService();
		System.out.println("Sending event : " + currentFlux.getType().toLowerCase() + "?" + currentFlux.getUrl() + "|" + String.join(",", macAddresses));
		eventController.send(
			currentFlux.getType().toLowerCase() +
				"?" +
				currentFlux.getUrl() +
				"|" +
				String.join(",", macAddresses)
		);

		// updating concerned screens
		for (String screenMac: macAddresses) {
			Screen screen = screenService.getScreenByMacAddress(screenMac);
			screen.setCurrentFluxName(currentFlux.getName());
			screenService.update(screen);
		}
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

	public void resendLastEvent() {
		if (lastEvent != null) {
			System.out.println("FORCE SEND");
			sendFluxEvent(lastEvent, null);
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
