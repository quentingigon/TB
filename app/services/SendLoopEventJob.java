package services;

import models.FluxEvent;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This class represents a Job associated with a FluxLoop.
 */
public class SendLoopEventJob implements EventJob {

	private FluxEvent event;
	private String source;
	private Integer entityId;

	public SendLoopEventJob() {

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
