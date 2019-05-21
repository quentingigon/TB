package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.RunningSchedule;

import java.util.List;

@ImplementedBy(JPARunningScheduleRepository.class)
public interface RunningScheduleRepository {

	RunningSchedule add(RunningSchedule schedule);

	RunningSchedule getById(Integer id);
	RunningSchedule getByScheduleId(Integer scheduleId);

	Integer getRunningScheduleIdByScreenId(Integer id);

	List<RunningSchedule> getAll();
	List<Integer> getScreensIdsByRunningScheduleId(Integer id);

	RunningSchedule update(RunningSchedule schedule);
	void delete(RunningSchedule schedule);
}
