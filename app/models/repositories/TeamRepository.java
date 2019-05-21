package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Team;

import java.util.List;

@ImplementedBy(JPATeamRepository.class)
public interface TeamRepository {

	Team add(Team team);
	Team getByName(String name);
	Team getById(Integer id);

	List<Team> getAll();

	Team update(Team team);
	void delete(Team team);
}
