package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.RunningSchedule;

import java.util.List;

@ImplementedBy(JPARunningScheduleRepository.class)
public interface RunningScheduleRepository {

	RunningSchedule add(RunningSchedule schedule);

	List<RunningSchedule> getAll();

	void update(RunningSchedule schedule);
	void delete(RunningSchedule schedule);
}
