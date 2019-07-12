package services;

import models.FluxEvent;
import models.db.Flux;
import models.db.FluxLoop;
import models.db.FluxTrigger;
import models.db.Schedule;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Comparator;
import java.util.List;

import static controllers.CronUtils.checkIfFluxHasSomethingToDisplayByDateTime;
import static controllers.CronUtils.mustFluxLoopBeStarted;

/**
 * This class implements a Listener for all SendEventJobs. After a Job was executed,
 * it transmits the event to the EventManager and then verify if a SendLoopEventJob must be started or not.
 */
public class SendEventJobsListener implements EventJobListener {

	private EventManager eventManager;
	private ServicePicker servicePicker;

	private String name;
	private SendEventJob lastJob;

	private boolean currentFluxHasNothingToDisplay;
	private boolean lastFluxHadNothingToDisplay;

	public SendEventJobsListener(String name,
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
		System.out.println("jobToBeExecuted");
		System.out.println("Job : " + jobName + " is going to start...");
	}

	@Override
	public void jobExecutionVetoed(JobExecutionContext context) {

	}

	/**
	 * Called after a Job was executed. Verify that the flux has something to display and in any case, calls
	 * the handleEvent() function of the EventManager. It then verify if a FluxLoop must be started or not.
	 * @param context context from which a DataMap is recovered containing the Job details
	 * @param jobException a potential exception
	 */
	@Override
	public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		JobDataMap triggerDataMap = context.getTrigger().getJobDataMap();
		SendEventJob job = (SendEventJob) context.getJobInstance();

		checkFlux(context.getJobDetail().getKey().toString(), job);

		lastJob = job;
		lastFluxHadNothingToDisplay = currentFluxHasNothingToDisplay;
		eventManager.handleEvent(job, currentFluxHasNothingToDisplay);

		List<FluxTrigger> triggers = servicePicker.getFluxService().getFluxTriggersOfScheduleById(job.getEntityId());
		triggers.sort(Comparator.comparing(FluxTrigger::getTime));

		List<FluxLoop> loops = servicePicker.getFluxService().getFluxLoopOfScheduleById(job.getEntityId());
		loops.sort(Comparator.comparing(FluxLoop::getStartTime));

		FluxEvent event = job.getEvent();
		Schedule schedule = servicePicker.getScheduleService().getScheduleById(job.getEntityId());

		Flux currentFlux = servicePicker.getFluxService().getFluxById(event.getFluxId());
		DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
		LocalTime timeAfterExecution = formatter.parseLocalTime(triggerDataMap.getString("time"))
			.plusMinutes(currentFlux.getTotalDuration());

		boolean isCurrentTriggerLast = true;
		for (FluxLoop loop: loops) {
			// if a FluxLoop is programmed for this time
			if (mustFluxLoopBeStarted(formatter.print(timeAfterExecution), loop, triggers)) {
				LoopEventJobCreator loopJobCreator = new LoopEventJobCreator(schedule, event.getScreenIds(), servicePicker, eventManager);
				loopJobCreator.createFromFluxLoop(loop);
				isCurrentTriggerLast = false;
			}
		}

		// if current trigger is last item in Schedule, start first loop of Schedule
		if (isCurrentTriggerLast && !isThereFluxTriggersAfterTime(formatter.print(timeAfterExecution), triggers)) {
			if (!loops.isEmpty()) {
				LoopEventJobCreator loopJobCreator = new LoopEventJobCreator(schedule, event.getScreenIds(), servicePicker, eventManager);
				loopJobCreator.createFromFluxLoop(loops.get(0));
			}
		}
	}

	private boolean isThereFluxTriggersAfterTime(String time, List<FluxTrigger> triggers) {
		for (FluxTrigger ft: triggers) {
			if (ft.getTime().compareTo(time) > 0) {
				return true;
			}
		}
		return false;
	}

	private void checkFlux(String jobName, SendEventJob job) {
		System.out.println("Checking job: " + jobName);
		currentFluxHasNothingToDisplay = !checkIfFluxHasSomethingToDisplayByDateTime(eventManager,
			servicePicker.getFluxService().getFluxById(job.getEvent().getFluxId()));
		System.out.println("Job : " + jobName + " has nothing to display ? -> " + currentFluxHasNothingToDisplay);
	}

	@Override
	public void resendLastEvent() {
		if (lastJob != null) {
			eventManager.handleEvent(lastJob, lastFluxHadNothingToDisplay);
		}
	}
}
