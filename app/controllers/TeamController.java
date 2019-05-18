package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.Team;
import models.entities.DataUtils;
import models.entities.TeamData;
import models.repositories.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.team.team_creation;
import views.html.team.team_page;
import views.html.team.team_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TeamController extends Controller {

	@Inject
	TeamRepository teamRepository;

	@Inject
	ScreenRepository screenRepository;

	@Inject
	FluxRepository fluxRepository;

	@Inject
	ScheduleRepository scheduleRepository;

	@Inject
	DiffuserRepository diffuserRepository;

	@Inject
	UserRepository userRepository;

	private Form<TeamData> form;

	private DataUtils dataUtils;

	@Inject
	public TeamController(FormFactory formFactory, DataUtils dataUtils) {
		this.dataUtils = dataUtils;
		this.form = formFactory.form(TeamData.class);
	}

	//@With(UserAuthentificationAction.class)
	public Result index() {
		return ok(team_page.render(dataUtils.getAllTeams(), null));
	}

	public Result indexWithErrorMessage(String error) {
		return ok(team_page.render(dataUtils.getAllTeams(), error));
	}

	//@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(team_creation.render(form,
			dataUtils.getAllFluxes(),
			dataUtils.getAllScreens(),
			dataUtils.getAllUsers(),
			dataUtils.getAllSchedules(),
			dataUtils.getAllDiffusers(),
			null));
	}

	private Result createViewWithErrorMessage(String error) {
		return badRequest(team_creation.render(form,
			dataUtils.getAllFluxes(),
			dataUtils.getAllScreens(),
			dataUtils.getAllUsers(),
			dataUtils.getAllSchedules(),
			dataUtils.getAllDiffusers(),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String teamName) {
		Team team = teamRepository.getByName(teamName);
		return ok(team_update.render(form,
			dataUtils.getAllFluxes(),
			dataUtils.getAllFluxesOfTeam(team.getId()),
			dataUtils.getAllScreens(),
			dataUtils.getAllScreensOfTeam(team.getId()),
			dataUtils.getAllUsers(),
			dataUtils.getAllMembersOfTeam(team.getId()),
			dataUtils.getAllSchedules(),
			dataUtils.getAllSchedulesOfTeam(team.getId()),
			dataUtils.getAllDiffusers(),
			dataUtils.getAllDiffusersOfTeam(team.getId()),
			new TeamData(team),
			null));
	}

	private Result updateViewWithErrorMessage(String teamName, String error) {
		Team team = teamRepository.getByName(teamName);
		return badRequest(team_update.render(form,
			dataUtils.getAllFluxes(),
			dataUtils.getAllFluxesOfTeam(team.getId()),
			dataUtils.getAllScreens(),
			dataUtils.getAllScreensOfTeam(team.getId()),
			dataUtils.getAllUsers(),
			dataUtils.getAllMembersOfTeam(team.getId()),
			dataUtils.getAllSchedules(),
			dataUtils.getAllSchedulesOfTeam(team.getId()),
			dataUtils.getAllDiffusers(),
			dataUtils.getAllDiffusersOfTeam(team.getId()),
			new TeamData(team),
			null));
	}

	// @With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<TeamData> boundForm = form.bindFromRequest(request);

		TeamData data = boundForm.get();

		if (teamRepository.getByName(data.getName()) != null) {
			return createViewWithErrorMessage("Team name already exists");
		}
		else {
			Team team = new Team(data.getName());

			Result error = checkDataIntegrity(data, "create");
			if (error != null) {
				return error;
			}

			fillTeamFromTeamData(team, data);

			teamRepository.add(team);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<TeamData> boundForm = form.bindFromRequest(request);

		TeamData data = boundForm.get();

		Team team = teamRepository.getByName(data.getName());

		if (team == null) {
			return updateViewWithErrorMessage(data.getName(), "Team name does not exists");
		}
		else {

			Result error = checkDataIntegrity(data, "update");
			if (error != null) {
				return error;
			}

			fillTeamFromTeamData(team, data);

			teamRepository.update(team);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String name) {
		Team team = teamRepository.getByName(name);

		if (team == null) {
			// team does not exists
			return indexWithErrorMessage("Team name does not exists");
		}
		else {
			teamRepository.delete(team);
			return index();
		}
	}

	private void fillTeamFromTeamData(Team team, TeamData data) {
		for (String fluxName: data.getFluxes()) {
			team.addToFluxes(fluxRepository.getByName(fluxName).getId());
		}

		for (String email: data.getMembers()) {
			team.addMember(userRepository.getByEmail(email).getId());
		}

		for (String email: data.getAdmins()) {
			team.addAdmin(userRepository.getByEmail(email).getId());
		}

		for (String scheduleName: data.getSchedules()) {
			team.addToSchedules(scheduleRepository.getByName(scheduleName).getId());
		}

		for (String diffuserName: data.getDiffusers()) {
			team.addToDiffusers(diffuserRepository.getByName(diffuserName).getId());
		}

		for (String screenMAC: data.getScreens()) {
			team.addScreen(screenRepository.getByMacAddress(screenMAC).getId());
		}
	}


	private Result checkDataIntegrity(TeamData data, String action) {

		Result error = null;

		if (data.getFluxes() != null) {
			for (String fluxName: data.getFluxes()) {
				if (fluxRepository.getByName(fluxName) == null) {
					if (action.equals("create"))
						error = createViewWithErrorMessage("Flux name does not exists");
					else if (action.equals("update"))
						error = updateViewWithErrorMessage(data.getName(), "Flux name does not exists");
				}
			}
		}
		else {
			data.setFluxes(new ArrayList<>());
		}

		if (data.getMembers() != null) {
			for (String email: data.getMembers()) {
				if (userRepository.getByEmail(email) == null) {
					if (action.equals("create"))
						error = createViewWithErrorMessage("User email address does not exists");
					else if (action.equals("update"))
						error = updateViewWithErrorMessage(data.getName(), "User email address does not exists");
				}
			}
		}
		else {
			data.setMembers(new ArrayList<>());
		}

		if (data.getAdmins() != null) {
			for (String email: data.getAdmins()) {
				if (userRepository.getByEmail(email) == null) {
					if (action.equals("create"))
						error = createViewWithErrorMessage("Flux name does not exists - admin");
					else if (action.equals("update"))
						error = updateViewWithErrorMessage(data.getName(), "Flux name does not exists - admin");
				}
			}
		}
		else {
			data.setAdmins(new ArrayList<>());
		}

		if (data.getSchedules() != null) {
			for (String scheduleName: data.getSchedules()) {
				if (scheduleRepository.getByName(scheduleName) == null) {
					if (action.equals("create"))
						error = createViewWithErrorMessage("Schedule name does not exists");
					else if (action.equals("update"))
						error = updateViewWithErrorMessage(data.getName(), "Schedule name does not exists");
				}
			}
		}
		else {
			data.setSchedules(new ArrayList<>());
		}

		if (data.getDiffusers() != null) {
			for (String diffuserName: data.getDiffusers()) {
				if (diffuserRepository.getByName(diffuserName) == null) {
					if (action.equals("create"))
						error = createViewWithErrorMessage("Diffuser name does not exists");
					else if (action.equals("update"))
						error = updateViewWithErrorMessage(data.getName(), "Diffuser name does not exists");
				}
			}
		}
		else {
			data.setDiffusers(new ArrayList<>());
		}

		if (data.getScreens() != null) {
			for (String screenMAC: data.getScreens()) {
				if (screenRepository.getByMacAddress(screenMAC) == null) {
					if (action.equals("create"))
						error = createViewWithErrorMessage("Screen MAC address does not exists");
					else if (action.equals("update"))
						error = updateViewWithErrorMessage(data.getName(), "Screen MAC address does not exists");
				}
			}
		}
		else {
			data.setScreens(new ArrayList<>());
		}
		return error;
	}
}
