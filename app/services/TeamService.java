package services;

import models.db.Team;
import models.entities.TeamData;
import models.repositories.TeamRepository;

import java.util.ArrayList;
import java.util.List;

public class TeamService {

	private final TeamRepository teamRepository;

	public TeamService(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	public Team create(Team team) {
		return teamRepository.add(team);
	}

	public Team update(Team team) {
		return teamRepository.update(team);
	}

	public List<TeamData> getAllTeams() {
		List<TeamData> data = new ArrayList<>();
		for (Team t: teamRepository.getAll()) {
			data.add(new TeamData(t));
		}
		return data;
	}
}
