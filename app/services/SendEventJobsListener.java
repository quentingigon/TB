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
import org.quartz.JobListener;

import java.util.Comparator;
import java.util.List;
import java.util.Observable;

import static controllers.CronUtils.mustFluxLoopBeStarted;

public class SendEventJobsListener extends Observable implements JobListener {

	private EventManager eventManager;
	private ServicePicker servicePicker;

	private String name;

	public SendEventJobsListener(String name,
								 EventManager eventManager,
								 ServicePicker servicePicker) {
		this.name = name;
		this.eventManager = eventManager;
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
		JobDataMap triggerDataMap = context.getTrigger().getJobDataMap();
		SendEventJob job = (SendEventJob) context.getJobInstance();

		eventManager.handleEvent(job);

		List<FluxTrigger> triggers = servicePicker.getFluxService().getFluxTriggersOfScheduleById(job.getScheduleId());
		triggers.sort(Comparator.comparing(FluxTrigger::getTime));

		List<FluxLoop> loops = servicePicker.getFluxService().getFluxLoopOfScheduleById(job.getScheduleId());
		loops.sort(Comparator.comparing(FluxLoop::getStartTime));

		FluxEvent event = job.getEvent();
		Schedule schedule = servicePicker.getScheduleService().getScheduleById(job.getScheduleId());

		Flux currentFlux = servicePicker.getFluxService().getFluxById(event.getFluxId());
		DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
		LocalTime timeAfterExecution = formatter.parseLocalTime(triggerDataMap.getString("time"))
			.plusMinutes(currentFlux.getTotalDuration());

		for (FluxLoop loop: loops) {
			// if a FluxLoop is programmed for this time
			if (mustFluxLoopBeStarted(formatter.print(timeAfterExecution), loop, triggers)) {
				LoopJobCreator loopJobCreator = new LoopJobCreator(schedule, event.getScreenIds(), servicePicker, eventManager);
				loopJobCreator.createFromFluxLoop(loop);
			}
		}
	}

	public EventManager getEventManager() {
		return eventManager;
	}
}
