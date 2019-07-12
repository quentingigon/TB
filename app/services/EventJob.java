package services;

import models.FluxEvent;
import org.quartz.Job;

/**
 * This interface represents an Event 
 */
public interface EventJob extends Job {

	FluxEvent getEvent();
	boolean isJobFromSchedule();
	int getEntityId();
}
