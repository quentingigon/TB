package models.repositories.interfaces;

import com.google.inject.ImplementedBy;
import models.db.Diffuser;
import models.repositories.JPADiffuserRepository;

import java.util.List;

@ImplementedBy(JPADiffuserRepository.class)
public interface DiffuserRepository {

	Diffuser add(Diffuser diffuser);
	Diffuser getByName(String name);
	Diffuser getById(Integer id);

	List<Diffuser> getAll();
	List<Integer> getAllDiffuserIdsOfTeam(Integer id);

	Diffuser update(Diffuser diffuser);
	void delete(Diffuser diffuser);
}
