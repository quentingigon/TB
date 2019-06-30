package services;

import models.FluxEvent;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendLoopEventJob implements Job, EventJob {

	private FluxEvent event;
	private String source;
	private int scheduleId;

	public SendLoopEventJob() {

	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap triggerDataMap = context.getTrigger().getJobDataMap();

		String screenIds = triggerDataMap.getString("screenIds");
		int currentFluxId = triggerDataMap.getInt("currentFluxId");

		source = triggerDataMap.getString("source");
		scheduleId = triggerDataMap.getInt("scheduleId");

		if (!screenIds.isEmpty()) {
			event = new FluxEvent(currentFluxId, screenIds);
		}
	}

	@Override
	public FluxEvent getEvent() {
		return event;
	}

	@Override
	public boolean isJobFromSchedule() {
		return source.equals("schedule");
	}

	@Override
	public int getScheduleId() {
		return scheduleId;
	}
}
