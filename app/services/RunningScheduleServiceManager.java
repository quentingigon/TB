package services;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class RunningScheduleServiceManager {

	private ExecutorService executorService;

	private static final RunningScheduleServiceManager instance = new RunningScheduleServiceManager();

	public RunningScheduleServiceManager() {
		this.executorService = Executors.newFixedThreadPool(10);
	}

	public void addRunningSchedule(Runnable r) {
		this.executorService.execute(r);
	}

	public static final RunningScheduleServiceManager getInstance()
	{
		return instance;
	}
}
