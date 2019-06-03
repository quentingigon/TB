public class ServicePicker {
	@Inject
	TeamRepository teamRepository;

	// + tout les autres repositories ...

	public TeamService getTeamService() {
		return new TeamService(teamRepository);
	}

	// + les autres ServiceGetters
}

public class TeamService {

	private final TeamRepository teamRepository;

	public TeamService(TeamRepository teamRepository) {
		this.teamRepository = teamRepository;
	}

	public Team getTeamByName(String name) {
		return teamRepository.getByName(name);
	}

	// + autres fonctions
}