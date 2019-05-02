package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.RunningDiffuser;

import java.util.List;

@ImplementedBy(JPARunningDiffuserRepository.class)
public interface RunningDiffuserRepository {

	RunningDiffuser add(RunningDiffuser diffuser);
	RunningDiffuser getByName(String name);

	List<RunningDiffuser> getAll();

	void update(RunningDiffuser diffuser);
	void delete(RunningDiffuser diffuser);
}
