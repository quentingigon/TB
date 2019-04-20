package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Flux;

@ImplementedBy(JPAFluxRepository.class)
public interface FluxRepository {

	Flux add(Flux flux);
	Flux getByName(String name);
	Flux getByUrl(String url);
	void update(Flux flux);
	void delete(Flux flux);
}
