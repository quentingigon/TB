package services;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Singleton
public class RunningScheduleThreadManager {

	private ExecutorService executorService;
	private HashMap<Integer, RunningScheduleThread> tasks;

	@Inject
	private RunningScheduleThreadManager() {
		this.executorService = Executors.newFixedThreadPool(10);
		this.tasks = new HashMap<>();
	}

	public void addRunningSchedule(Integer scheduleId, RunningScheduleThread r) {
		tasks.put(scheduleId, r);
		executorService.submit(r);
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