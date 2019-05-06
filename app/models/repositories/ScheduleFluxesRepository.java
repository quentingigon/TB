package models.repositories;

import com.google.inject.ImplementedBy;

import java.util.List;

@ImplementedBy(JPAScheduleFluxesRepository.class)
public interface ScheduleFluxesRepository {

	List<Integer> getFluxesIdsByScheduleId(Integer id);
}
