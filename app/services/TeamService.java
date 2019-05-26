package services;

import models.db.Team;
import models.entities.TeamData;
import models.repositories.interfaces.TeamRepository;

import java.util.ArrayList;
import java.util.List;

public class TeamService {

	private final TeamRepository teamRepository;

	public TeamService(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	public Team getTeamByName(String name) {
		return teamRepository.getByName(name);
	}

	public Team getTeamById(Integer id) {
		return teamRepository.getById(id);
	}

	public Team create(Team team) {
		return teamRepository.add(team);
	}

	public Team update(Team team) {
		return teamRepository.update(team);
	}

	public void delete(Team team) {
		teamRepository.delete(team);
	}

	public List<TeamData> getAllTeams() {
		List<TeamData> data = new ArrayList<>();
		for (Team t: teamRepository.getAll()) {
			data.add(new TeamData(t));
		}
		return data;
	}
}
