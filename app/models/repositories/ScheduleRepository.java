package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.RunningSchedule;
import models.db.Schedule;

@ImplementedBy(JPAScheduleRepository.class)
public interface ScheduleRepository {

	Schedule add(Schedule schedule);
	void update(Schedule schedule);
	void delete(Schedule schedule);
	Schedule getByName(String name);

	void activate(RunningSchedule schedule);
}
