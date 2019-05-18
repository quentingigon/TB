package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.RunningSchedule;

import java.util.List;

@ImplementedBy(JPARunningScheduleRepository.class)
public interface RunningScheduleRepository {

	RunningSchedule add(RunningSchedule schedule);

	RunningSchedule getById(Integer id);
	RunningSchedule getByScheduleId(Integer scheduleId);

	List<RunningSchedule> getAll();
	List<Integer> getScreensIdsByRunningScheduleId(Integer id);

	void update(RunningSchedule schedule);
	void delete(RunningSchedule schedule);
}
