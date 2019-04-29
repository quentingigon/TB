package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Flux;

import java.util.List;

@ImplementedBy(JPAFluxRepository.class)
public interface FluxRepository {

	Flux add(Flux flux);
	Flux getByName(String name);
	Flux getByUrl(String url);

	List<Flux> getAll();

	void update(Flux flux);
	void delete(Flux flux);
}
