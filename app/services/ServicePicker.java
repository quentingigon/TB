package services;

import models.repositories.interfaces.*;

import javax.inject.Inject;

/**
 * This is the class that gives access to the different services using dependency injection
 */
public class ServicePicker {

	@Inject
	TeamRepository teamRepository;

	@Inject
	ScreenRepository screenRepository;

	@Inject
	FluxRepository fluxRepository;

	@Inject
	ScheduleRepository scheduleRepository;

	@Inject
	RunningScheduleRepository runningScheduleRepository;

	@Inject
	RunningDiffuserRepository runningDiffuserRepository;

	@Inject
	DiffuserRepository diffuserRepository;

	@Inject
	UserRepository userRepository;

	public FluxService getFluxService() {
		return new FluxService(fluxRepository);
	}

	public ScreenService getScreenService() {
		return new ScreenService(screenRepository);
	}

	public ScheduleService getScheduleService() {
		return new ScheduleService(scheduleRepository, runningScheduleRepository);
	}

	public DiffuserService getDiffuserService() {
		return new DiffuserService(diffuserRepository, runningDiffuserRepository);
	}

	public UserService getUserService() {
		return new UserService(userRepository);
	}

	public TeamService getTeamService() {
		return new TeamService(teamRepository);
	}
}
