package models.repositories.interfaces;

import com.google.inject.ImplementedBy;
import models.db.RunningSchedule;
import models.db.Schedule;
import models.db.ScheduledFlux;
import models.repositories.JPAScheduleRepository;

import java.util.List;

@ImplementedBy(JPAScheduleRepository.class)
public interface ScheduleRepository {

	Schedule add(Schedule schedule);
	Schedule getByName(String name);
	Schedule getById(Integer id);

	ScheduledFlux addScheduledFlux(ScheduledFlux scheduledFlux);

	List<Schedule> getAll();
	List<Integer> getAllScheduleIdsOfTeam(Integer id);

	Schedule update(Schedule schedule);
	void delete(Schedule schedule);
	void activate(RunningSchedule schedule);
}
