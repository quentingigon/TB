package services;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class RunningScheduleServiceManager {

	private ExecutorService executorService;
	private HashMap<String, RunningScheduleService> tasks;

	private static final RunningScheduleServiceManager instance = new RunningScheduleServiceManager();

	private RunningScheduleServiceManager() {
		this.executorService = Executors.newFixedThreadPool(10);
		this.tasks = new HashMap<>();
	}

	public void addRunningSchedule(String scheduleName, RunningScheduleService r) {
		executorService.submit(r);
		tasks.put(scheduleName, r);
	}

	public void removeRunningSchedule(String scheduleName) {
		tasks.remove(scheduleName).setRunning(false);
	}

	public RunningScheduleService getServiceByName(String name) {
		return tasks.get(name);
	}

	public static final RunningScheduleServiceManager getInstance()
	{
		return instance;
	}
}
