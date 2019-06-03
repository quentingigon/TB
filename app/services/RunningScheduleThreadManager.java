package services;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class is the manager for the RunningScheduleThreads and allows to start/stop them
 * and to get them.
 */
@Singleton
public class RunningScheduleThreadManager {

	private final int MAX_THREADS = 20;

	private ExecutorService executorService;
	private HashMap<Integer, RunningScheduleThread> tasks;

	@Inject
	private RunningScheduleThreadManager() {
		this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
		this.tasks = new HashMap<>();
	}

	public void addRunningScheduleThread(Integer scheduleId, RunningScheduleThread rst) {
		tasks.put(scheduleId, rst);
		executorService.submit(rst);
		System.out.println("Schedule activated");
	}

	public void removeRunningSchedule(Integer scheduleId) {
		System.out.println("Deactivated service : " + tasks.get(scheduleId));
		if (tasks.get(scheduleId) != null) {
			tasks.remove(scheduleId).setRunning(false);
		}
	}

	public RunningScheduleThread getServiceByScheduleId(Integer id) {
		return tasks.getOrDefault(id, null);
	}
}
