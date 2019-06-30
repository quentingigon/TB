package services;

import models.FluxEvent;
import org.quartz.Job;

public interface EventJob extends Job {

	FluxEvent getEvent();
	boolean isJobFromSchedule();
	int getScheduleId();
}
