package services;

import models.db.Schedule;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Observable;

import static controllers.CronUtils.checkIfFluxHasSomethingToDisplay;

public class SendLoopEventJobsListener extends Observable implements EventJobListener {

	private EventManager eventManager;
	private ServicePicker servicePicker;

	private String name;
	private SendLoopEventJob lastJob;

	private boolean currentFluxHasNothingToDisplay;
	private boolean lastFluxHadNothingToDisplay;

	public SendLoopEventJobsListener(String name,
									 EventManager eventManager,
									 ServicePicker servicePicker) {
		this.name = name;
		this.eventManager = eventManager;
		this.servicePicker = servicePicker;

		currentFluxHasNothingToDisplay = false;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void jobToBeExecuted(JobExecutionContext context) {
		String jobName = context.getJobDetail().getKey().toString();
		System.out.println("jobToBeExecuted : loop");
		System.out.println("Job : " + jobName + " is going to start...");
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {

	}

	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {

		JobDataMap triggerDataMap = context.getTrigger().getJobDataMap();

		SendLoopEventJob job = (SendLoopEventJob) context.getJobInstance();

		checkFlux(context.getJobDetail().getKey().toString(), job);

		lastJob = job;
		lastFluxHadNothingToDisplay = currentFluxHasNothingToDisplay;

		eventManager.handleEvent(job, currentFluxHasNothingToDisplay);

		Schedule schedule = servicePicker.getScheduleService().getScheduleById(job.getEntityId());

		createNextLoopJob(triggerDataMap, schedule);
	}

	private void checkFlux(String jobName, SendLoopEventJob job) {
		System.out.println("Checking job: " + jobName);
		currentFluxHasNothingToDisplay = !checkIfFluxHasSomethingToDisplay(eventManager,
			servicePicker.getFluxService().getFluxById(job.getEvent().getFluxId()));
		System.out.println("Job : " + jobName + " has nothing to display ? -> " + currentFluxHasNothingToDisplay);
	}

	private void createNextLoopJob(JobDataMap triggerDataMap, Schedule schedule) {

		LoopEventJobCreator loopJobCreator = new LoopEventJobCreator(schedule,
			triggerDataMap.getString("screenIds"),
			servicePicker,
			eventManager);

		loopJobCreator.createFromJob(triggerDataMap);
	}

	@Override
	public void resendLastEvent() {
		eventManager.handleEvent(lastJob, lastFluxHadNothingToDisplay);
	}
}
