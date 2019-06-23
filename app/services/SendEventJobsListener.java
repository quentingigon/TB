package services;

import controllers.EventSourceController;
import models.FluxEvent;
import models.db.Flux;
import models.db.RunningDiffuser;
import models.db.Screen;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.util.*;

public class SendEventJobsListener extends Observable implements JobListener {

	private EventSourceController eventController;
	private ServicePicker servicePicker;

	private String name;
	private FluxEvent lastEvent;

	public SendEventJobsListener(String name,
								 EventSourceController eventController,
								 ServicePicker servicePicker) {
		this.name = name;
		this.eventController = eventController;
		this.servicePicker = servicePicker;
		lastEvent = null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		String jobName = context.getJobDetail().getKey().toString();
		System.out.println("jobToBeExecuted");
		System.out.println("Job : " + jobName + " is going to start...");
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {

	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		SendEventJob job = (SendEventJob) context.getJobInstance();

		FluxEvent event = job.getEvent();

		sendFluxEventAsGeneralOrLocated(event);
	}

	private void sendFluxEventAsGeneralOrLocated(FluxEvent fluxEvent) {
		FluxService fluxService = servicePicker.getFluxService();
		Flux currentFlux = fluxService.getFluxById(fluxEvent.getFluxId());

		// current flux is a located one
		if (fluxService.getLocatedFluxByFluxId(currentFlux.getId()) != null) {
			int siteId = fluxService.getLocatedFluxByFluxId(currentFlux.getId()).getSiteId();

			// lists of screens to send the flux to
			List<String> screenIdsWithSameSiteId = new ArrayList<>(Collections.singletonList(fluxEvent.getScreenIds()));
			List<String> screenIdsWithDifferentSiteId = new ArrayList<>();

			List<Screen> screens = getScreenList(fluxEvent.getScreenIds());

			for (Screen s : screens) {
				// if flux and screen are not restricted to the same site
				if (siteId != s.getSiteId()) {
					screenIdsWithDifferentSiteId.add(String.valueOf(s.getId()));
					screenIdsWithSameSiteId.remove(String.valueOf(s.getId()));
				}
			}

			// all screens are related to the same site as the flux
			if (screenIdsWithDifferentSiteId.isEmpty()) {
				// send event to observer
				sendFluxEvent(fluxEvent);
			}
			else {
				StringBuilder screenIdsWithSameSiteIdBis = new StringBuilder();
				for (String s : screenIdsWithSameSiteId)
				{
					screenIdsWithSameSiteIdBis.append(s);
				}
				sendFluxEvent(new FluxEvent(fluxEvent.getFluxId(), screenIdsWithSameSiteIdBis.toString()));

				StringBuilder screenIdsWithDifferentSiteIdBis = new StringBuilder();
				for (String s : screenIdsWithDifferentSiteId)
				{
					screenIdsWithDifferentSiteIdBis.append(s);
				}
				sendFluxEvent(new FluxEvent(fluxEvent.getFluxId(), screenIdsWithDifferentSiteIdBis.toString()));
			}
		}
		// current flux is a general one
		else {
			sendFluxEvent(fluxEvent);
		}
	}

	private String sendDiffusedEventToConcernedScreens(FluxEvent event) {
		StringBuilder screenIdsWithNoDiffuser = new StringBuilder();
		StringBuilder screenIdsWithDiffuser = new StringBuilder();
		List<Screen> screens = getScreenList(event.getScreenIds());
		for (Screen s: screens) {
			RunningDiffuser rd = getRunningDiffuserIfPresent(s);
			// TODO check time and duration of Diffuser
			if (rd != null) {
				screenIdsWithDiffuser.append(s.getId());
			}
			else {
				screenIdsWithNoDiffuser.append(s.getId());
			}
		}

		if (!screenIdsWithDiffuser.toString().isEmpty()) {
			// send diffused event
			send(servicePicker.getFluxService().getFluxById(event.getFluxId()),
				getMacAddresses(screenIdsWithDiffuser.toString()));
		}


		return screenIdsWithNoDiffuser.toString();
	}

	private void sendFluxEvent(FluxEvent event) {
		Flux currentFlux = getFlux(event.getFluxId());

		if (currentFlux != null) {

			// send diffused flux to correct screen and returns the ids of the other screens
			String screenIdsWithNoActiveDiffuser = sendDiffusedEventToConcernedScreens(event);

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
			return  diffuserService.getRunningDiffuserById(runningDiffuserId);
		}
		else {
			return null;
		}
	}

	public void resendLastEvent() {
		if (lastEvent != null) {
			System.out.println("FORCE SEND");
			sendFluxEvent(lastEvent);
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
}
