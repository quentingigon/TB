package services;

import models.db.Schedule;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.util.Observable;

public class SendLoopEventJobsListener extends Observable implements JobListener {

	private EventManager eventManager;
	private ServicePicker servicePicker;

	private String name;

	public SendLoopEventJobsListener(String name,
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

		SendLoopEventJob job = (SendLoopEventJob) context.getJobInstance();

		eventManager.handleEvent(job);

		Schedule schedule = servicePicker.getScheduleService().getScheduleById(job.getEntityId());

		createNextLoopJob(triggerDataMap, schedule);
	}

	private void createNextLoopJob(JobDataMap triggerDataMap, Schedule schedule) {

		LoopJobCreator loopJobCreator = new LoopJobCreator(schedule,
			triggerDataMap.getString("screenIds"),
			servicePicker,
			eventManager);

		loopJobCreator.createFromJob(triggerDataMap);
	}
}
