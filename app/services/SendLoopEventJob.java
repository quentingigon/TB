package services;

import models.FluxEvent;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SendLoopEventJob implements EventJob {

	private FluxEvent event;
	private String source;
	private int entityId;

	public SendLoopEventJob() {

	}

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {

		JobDataMap triggerDataMap = context.getTrigger().getJobDataMap();

		String screenIds = triggerDataMap.getString("screenIds");
		int currentFluxId = triggerDataMap.getInt("currentFluxId");

		source = triggerDataMap.getString("source");
		entityId = triggerDataMap.getInt("entityId");

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
	public int getEntityId() {
		return entityId;
	}
}
