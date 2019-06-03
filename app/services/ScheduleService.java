package services;

import models.db.RunningSchedule;
import models.db.Schedule;
import models.entities.ScheduleData;
import models.repositories.interfaces.RunningScheduleRepository;
import models.repositories.interfaces.ScheduleRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the service used to make operations on the database for Schedules.
 */
public class ScheduleService {

	private final ScheduleRepository scheduleRepository;
	private final RunningScheduleRepository runningScheduleRepository;

	public ScheduleService(ScheduleRepository scheduleRepository,
						   RunningScheduleRepository runningScheduleRepository) {
		this.scheduleRepository = scheduleRepository;
		this.runningScheduleRepository = runningScheduleRepository;
	}

	public Schedule getScheduleById(Integer id) {
		return scheduleRepository.getById(id);
	}

	public Schedule getScheduleByName(String name) {
		return scheduleRepository.getByName(name);
	}

	public Schedule create(Schedule schedule) {
		return scheduleRepository.add(schedule);
	}

	public Schedule update(Schedule schedule) {
		return scheduleRepository.update(schedule);
	}

	public void delete(Schedule schedule) {
		scheduleRepository.delete(schedule);
	}

	public RunningSchedule getRunningScheduleById(Integer id) {
		return runningScheduleRepository.getById(id);
	}

	public RunningSchedule getRunningScheduleByScheduleId(Integer id) {
		return runningScheduleRepository.getByScheduleId(id);
	}

	public Integer getRunningScheduleOfScreenById(Integer id) {
		return runningScheduleRepository.getRunningScheduleIdByScreenId(id);
	}

	public RunningSchedule create(RunningSchedule rs) {
		return runningScheduleRepository.add(rs);
	}

	public RunningSchedule update(RunningSchedule rs) {
		return runningScheduleRepository.update(rs);
	}

	public void delete(RunningSchedule rs) {
		runningScheduleRepository.delete(rs);
	}

	public List<ScheduleData> getAllSchedules() {
		List<ScheduleData> data = new ArrayList<>();
		for (Schedule s: scheduleRepository.getAll()) {
			data.add(new ScheduleData(s));
		}
		return data;
	}

	public List<ScheduleData> getAllSchedulesOfTeam(int teamId) {
		List<ScheduleData> data = new ArrayList<>();
		for (Integer scheduleId : scheduleRepository.getAllScheduleIdsOfTeam(teamId)) {
			if (scheduleRepository.getById(scheduleId) != null) {
				ScheduleData sd = new ScheduleData(scheduleRepository.getById(scheduleId));

				// if schedule is activated
				if (runningScheduleRepository.getByScheduleId(scheduleId) != null) {
					sd.setActivated(true);
				}
				data.add(sd);
			}
		}
		return data;
	}

	public List<ScheduleData> getAllActiveSchedulesOfTeam(int teamId) {
		List<ScheduleData> schedules = new ArrayList<>();
		for (ScheduleData sd: getAllSchedulesOfTeam(teamId)) {
			Schedule schedule = scheduleRepository.getByName(sd.getName());

			if (runningScheduleRepository.getByScheduleId(schedule.getId()) != null) {
				sd.setActivated(true);
				schedules.add(sd);
			}
		}
		return schedules;
	}

	public List<Integer> getAllScreenIdsOfRunningScheduleById(int runningScheduleId) {
		return new ArrayList<>(runningScheduleRepository.getScreensIdsByRunningScheduleId(runningScheduleId));
	}
}
