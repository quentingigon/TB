package services;

import controllers.EventSourceController;
import models.FluxEvent;
import models.db.Flux;
import models.db.Screen;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;

public class SendEventJobsListener extends Observable implements JobListener {

	private EventSourceController eventController;
	private ServicePicker servicePicker;

	private String name;

	public SendEventJobsListener(String name,
								 EventSourceController eventController,
								 ServicePicker servicePicker) {
		this.name = name;
		this.eventController = eventController;
		this.servicePicker = servicePicker;
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
		ScreenService screenService = servicePicker.getScreenService();

		SendEventJob job = (SendEventJob) context.getJobInstance();

		FluxEvent event = job.getEvent();

		Flux currentFlux = getFlux(event.getFluxId());
		List<String> macAddresses = getMacAddresses(event.getScreenIds());

		if (currentFlux != null) {
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
		List<String> list = Arrays.asList(screenIds.split("\\s*,\\s*"));
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
