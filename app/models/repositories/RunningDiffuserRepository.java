package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.RunningDiffuser;

import java.util.List;

@ImplementedBy(JPARunningDiffuserRepository.class)
public interface RunningDiffuserRepository {

	RunningDiffuser add(RunningDiffuser diffuser);
	RunningDiffuser getByName(String name);
	RunningDiffuser getByDiffuserId(Integer id);

	List<RunningDiffuser> getAll();
	List<Integer> getScreenIdsOfRunningDiffuser(Integer id);

	void update(RunningDiffuser diffuser);
	void delete(RunningDiffuser diffuser);
}
