package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Diffuser;

import java.util.List;

@ImplementedBy(JPADiffuserRepository.class)
public interface DiffuserRepository {

	Diffuser add(Diffuser diffuser);
	Diffuser getByName(String name);
	Diffuser getById(Integer id);

	List<Diffuser> getAll();
	List<Integer> getAllDiffuserIdsOfTeam(Integer id);

	void update(Diffuser diffuser);
	void delete(Diffuser diffuser);
}
