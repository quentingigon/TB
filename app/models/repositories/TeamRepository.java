package models.repositories;

import com.google.inject.ImplementedBy;
import models.db.Team;

@ImplementedBy(JPATeamRepository.class)
public interface TeamRepository {

	Team add(Team team);
	Team getByName(String name);
	void update(Team team);
}
