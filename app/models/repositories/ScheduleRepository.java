package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Schedule;

@ImplementedBy(JPAScheduleRepository.class)
public interface ScheduleRepository {

	Schedule add(Schedule schedule);
	Schedule getByName(String name);
}
