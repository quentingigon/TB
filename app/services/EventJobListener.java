package services;

import org.quartz.JobListener;

/**
 * This interface represents a Listener of EventJob
 */
public interface EventJobListener extends JobListener {

	void resendLastEvent();
}
