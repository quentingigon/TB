package services;

import models.db.RunningSchedule;
import models.db.Schedule;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Observable;

import static controllers.CronUtils.checkIfFluxHasSomethingToDisplayByDateTime;

/**
 * This class implements a Listener for all SendLoopEventJobs. After a Job was executed,
 * it transmits the event to the EventManager and try to create a new SendLoopEventJob
 * with the next flux of the FluxLoop.
 */
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

	/**
	 * Called after a Job was executed. Verify that the flux has something to display and in any case, calls
	 * the handleEvent() function of the EventManager. It then try to create a new SendLoopEventJob with the next flux of the loop.
	 * @param context context from which a DataMap is recovered containing the Job details
	 * @param jobException a potential exception
	 */
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
		currentFluxHasNothingToDisplay = !checkIfFluxHasSomethingToDisplayByDateTime(eventManager,
			servicePicker.getFluxService().getFluxById(job.getEvent().getFluxId()));
		System.out.println("Job : " + jobName + " has nothing to display ? -> " + currentFluxHasNothingToDisplay);
	}

	private void createNextLoopJob(JobDataMap triggerDataMap, Schedule schedule) {

		RunningSchedule rs = servicePicker.getScheduleService().getRunningScheduleByScheduleId(schedule.getId());
		StringBuilder screenIds = new StringBuilder();

		for (Integer screenId: servicePicker.getScheduleService().getAllScreenIdsOfRunningScheduleById(rs.getId())) {
			screenIds.append(screenId).append(",");
		}
		if (!screenIds.toString().isEmpty()) {
			screenIds.deleteCharAt(screenIds.length() - 1);
		}

		LoopEventJobCreator loopJobCreator = new LoopEventJobCreator(schedule,
			screenIds.toString(),
			servicePicker,
			eventManager);

		loopJobCreator.createFromJob(triggerDataMap);
	}

	@Override
	public void resendLastEvent() {
		eventManager.handleEvent(lastJob, lastFluxHadNothingToDisplay);
	}
}
