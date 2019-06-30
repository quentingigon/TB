package services;

import controllers.EventSourceController;
import models.FluxEvent;
import models.db.*;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Singleton
public class EventManager {

	private final EventSourceController eventController;
	private final ServicePicker servicePicker;

	private FluxEvent lastEvent;

	@Inject
	public EventManager(EventSourceController eventSourceController,
						ServicePicker servicePicker) {
		this.eventController = eventSourceController;
		this.servicePicker = servicePicker;
		lastEvent = null;
	}

	public void handleEvent (EventJob job) {
		FluxEvent event = job.getEvent();

		// there is an active schedule for the screens concerned by the job
		if (job.isJobFromSchedule()) {
			sendFluxEventAsGeneralOrLocated(event, job.getScheduleId());
		}
		// job is from a diffuser
		else {
			sendFluxEvent(event, job.getScheduleId());
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

				Random rand = new Random();
				List<Integer> fallbackIds = fluxService
					.getFallBackIdsOfScheduleById(scheduleId);

				// send a fallback flux if wrong location
				if (!fallbackIds.isEmpty()) {
					Flux fallback = fluxService
						.getFluxById(fallbackIds
							.get(rand.nextInt(fallbackIds.size())));
					sendFluxEvent(new FluxEvent(fallback.getId(), screenIdsWithDifferentSiteIdAsString.toString()), scheduleId);
				}
			}
		}
		// current flux is a general one
		else {
			sendFluxEvent(fluxEvent, scheduleId);
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

	private String sendDiffusedEventToConcernedScreens(FluxEvent event, Integer scheduleId) {
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

					// TODO add check time overlap
					if (checkIfScheduleAndDiffuserDaysOverlap(schedule, diffuser)) {
						send(servicePicker.getFluxService().getFluxById(diffuser.getFlux()),
							new ArrayList<>(Collections.singletonList(String.valueOf(s.getId()))));
					}
				}
				// no active schedule, just a diffuser
				else {
					send(servicePicker.getFluxService().getFluxById(diffuser.getFlux()),
						new ArrayList<>(Collections.singletonList(String.valueOf(s.getId()))));
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

		// String currentDate = new SimpleDateFormat("yyyy-dd-MM").format(new Date());
		String diffuserDateString = diffuser.getTime();
		SimpleDateFormat df = new SimpleDateFormat("HH:mm");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		Date diffuserDate = null;
		try {
			diffuserDate = df.parse(diffuserDateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		long diffuserTime = diffuserDate.getTime() + (diffusedFlux.getTotalDuration() * 60000);

		for (FluxTrigger ft: triggers) {

			String fluxTriggerDateString = ft.getTime();
			Date fluxTriggerDate = null;
			try {
				fluxTriggerDate = df.parse(fluxTriggerDateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (diffuserTime - fluxTriggerDate.getTime() > 0 &&
				diffuserTime - fluxTriggerDate.getTime() < diffusedFlux.getTotalDuration()) {
				return true;
			}
		}
		return false;
	}

	private void sendFluxEvent(FluxEvent event, Integer scheduleId) {
		Flux currentFlux = getFlux(event.getFluxId());

		if (currentFlux != null) {

			// send diffused flux to correct screen and returns the ids of the other screens
			String screenIdsWithNoActiveDiffuser = sendDiffusedEventToConcernedScreens(event, scheduleId);

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

	private boolean checkIfScheduleAndDiffuserDaysOverlap(Schedule schedule, Diffuser diffuser) {
		String[] existingDays = schedule.getDays().split(",");
		String[] newDays = diffuser.getDays().split(",");

		boolean output = false;

		for (String day: newDays) {
			if (Arrays.asList(existingDays).contains(day)) {
				output = true;
			}
		}
		return output;
	}
}
