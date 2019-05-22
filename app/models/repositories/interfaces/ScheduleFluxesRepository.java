package models.repositories.interfaces;

import com.google.inject.ImplementedBy;
import models.db.ScheduledFlux;
import models.repositories.JPAScheduleFluxesRepository;

import java.util.List;

@ImplementedBy(JPAScheduleFluxesRepository.class)
public interface ScheduleFluxesRepository {

	ScheduledFlux getById(Integer id);

	List<Integer> getFluxesIdsByScheduleId(Integer id);
}
