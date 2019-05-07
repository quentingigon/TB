package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;

import java.util.List;

@ImplementedBy(JPAFluxRepository.class)
public interface FluxRepository {

	Flux addFlux(Flux flux);
	LocatedFlux addLocatedFlux(LocatedFlux flux);
	GeneralFlux addGeneralFlux(GeneralFlux flux);

	Flux getByName(String name);
	Flux getById(Integer id);

	List<Flux> getAll();

	void update(Flux flux);
	void delete(Flux flux);
}
