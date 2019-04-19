package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Diffuser;

@ImplementedBy(JPADiffuserRepository.class)
public interface DiffuserRepository {

	Diffuser add(Diffuser diffuser);
	Diffuser getByName(String name);
}
