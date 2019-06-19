package services;

import models.FluxEvent;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Observable;

public class SendEventJob extends Observable implements Job {

	private FluxEvent event;

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
	}

	public FluxEvent getEvent() {
		return event;
	}
}
