package services;

import org.quartz.JobListener;

public interface EventJobListener extends JobListener {

	EventManager getEventManager();
}
