package services;

import models.FluxEvent;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendEventJob implements EventJob {

	private FluxEvent event;
	private int scheduleId;
	private int diffuserId;
	private String source;

	public SendEventJob() {

	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap triggerDataMap = context.getTrigger().getJobDataMap();

		String screenIds = triggerDataMap.getString("screenIds");
		int fluxId = triggerDataMap.getInt("fluxId");

		// if there is still a screen to send the event to
		if (!screenIds.isEmpty()) {
			event = new FluxEvent(fluxId, screenIds);
		}

		source = triggerDataMap.getString("source");

		if (source.equals("schedule")) {
			scheduleId = triggerDataMap.getInt("scheduleId");
		}
		else {
			diffuserId = triggerDataMap.getInt("diffuserId");
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

	public int getScheduleId() {
		return scheduleId;
	}

	public int getDiffuserId() {
		return diffuserId;
	}
}
