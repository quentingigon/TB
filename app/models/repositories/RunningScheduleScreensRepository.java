package models.repositories;

import com.google.inject.ImplementedBy;

import java.util.List;

@ImplementedBy(JPARunningScheduleScreensRepository.class)
public interface RunningScheduleScreensRepository {

	List<Integer> getScreensIdsByRunningScheduleId(Integer id);
}
