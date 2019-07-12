package services;

import models.FluxEvent;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This class represents a Job associated with a FluxTrigger.
 */
public class SendEventJob implements EventJob {

	private FluxEvent event;
	private int entityId;
	private String source;

	public SendEventJob() {

	}

	/**
	 * Main function of the job, executed when the associated Trigger fires up.
	 * @param context context from which a DataMap is recovered
	 * @throws JobExecutionException
	 */
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
		entityId = triggerDataMap.getInt("entityId");
	}

	@Override
	public FluxEvent getEvent() {
		return event;
	}

	@Override
	public boolean isJobFromSchedule() {
		return source.equals("schedule");
	}

	public int getEntityId() {
		return entityId;
	}
}
