package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.RunningSchedule;
import models.db.Schedule;

import java.util.List;

@ImplementedBy(JPAScheduleRepository.class)
public interface ScheduleRepository {

	Schedule add(Schedule schedule);
	Schedule getByName(String name);
	Schedule getById(Integer id);

	List<Schedule> getAll();
	List<Integer> getAllScheduleIdsOfTeam(Integer id);

	void update(Schedule schedule);
	void delete(Schedule schedule);
	void activate(RunningSchedule schedule);
}
