package models.repositories.interfaces;

import com.google.inject.ImplementedBy;
import models.db.RunningDiffuser;
import models.repositories.JPARunningDiffuserRepository;

import java.util.List;

/**
 * This interface defines the functions for RunningDiffuser database operations.
 */
@ImplementedBy(JPARunningDiffuserRepository.class)
public interface RunningDiffuserRepository {

	RunningDiffuser add(RunningDiffuser diffuser);
	RunningDiffuser getByName(String name);
	RunningDiffuser getById(Integer id);
	RunningDiffuser getByDiffuserId(Integer id);

	Integer getRunningDiffuserIdOfScreenById(Integer id);

	List<RunningDiffuser> getAll();
	List<Integer> getScreenIdsOfRunningDiffuser(Integer id);

	RunningDiffuser update(RunningDiffuser diffuser);
	void delete(RunningDiffuser diffuser);
}
