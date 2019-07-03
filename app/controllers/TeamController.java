package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.Team;
import models.entities.TeamData;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.ServicePicker;
import services.TeamService;
import views.html.team.team_creation;
import views.html.team.team_page;
import views.html.team.team_update;

import javax.inject.Inject;
import java.util.ArrayList;

/**
 * This class implements a controller for the Teams.
 * It gives CRUD operations.
 */
public class TeamController extends Controller {

	private Form<TeamData> form;

	private final ServicePicker servicePicker;

	@Inject
	public TeamController(FormFactory formFactory,
						  ServicePicker servicePicker) {
		this.servicePicker = servicePicker;
		this.form = formFactory.form(TeamData.class);
	}

	//@With(UserAuthentificationAction.class)
	public Result index() {
		return ok(team_page.render(servicePicker.getTeamService().getAllTeams(),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result indexWithErrorMessage(String error) {
		return ok(team_page.render(servicePicker.getTeamService().getAllTeams(),
			error));
	}

	//@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(team_creation.render(form,
			servicePicker.getFluxService().getAllFluxes(),
			servicePicker.getScreenService().getAllScreens(),
			servicePicker.getUserService().getAllUsers(),
			servicePicker.getScheduleService().getAllSchedules(),
			servicePicker.getDiffuserService().getAllDiffusers(),
			null));
	}

	@With(UserAuthentificationAction.class)
	private Result createViewWithErrorMessage(String error) {
		return badRequest(team_creation.render(form,
			servicePicker.getFluxService().getAllFluxes(),
			servicePicker.getScreenService().getAllScreens(),
			servicePicker.getUserService().getAllUsers(),
			servicePicker.getScheduleService().getAllSchedules(),
			servicePicker.getDiffuserService().getAllDiffusers(),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String teamName) {
		Team team = servicePicker.getTeamService().getTeamByName(teamName);
		return ok(team_update.render(form,
			servicePicker.getFluxService().getAllFluxes(),
			servicePicker.getFluxService().getAllFluxesOfTeam(team.getId()),
			servicePicker.getScreenService().getAllScreens(),
			servicePicker.getScreenService().getAllScreensOfTeam(team.getId()),
			servicePicker.getUserService().getAllUsers(),
			servicePicker.getUserService().getAllMembersOfTeam(team.getId()),
			servicePicker.getUserService().getAllAdminsOfTeam(team.getId()),
			servicePicker.getScheduleService().getAllSchedules(),
			servicePicker.getScheduleService().getAllSchedulesOfTeam(team.getId()),
			servicePicker.getDiffuserService().getAllDiffusers(),
			servicePicker.getDiffuserService().getAllDiffusersOfTeam(team.getId()),
			new TeamData(team),
			null));
	}

	@With(UserAuthentificationAction.class)
	private Result updateViewWithErrorMessage(String teamName, String error) {
		Team team = servicePicker.getTeamService().getTeamByName(teamName);
		return badRequest(team_update.render(form,
			servicePicker.getFluxService().getAllFluxes(),
			servicePicker.getFluxService().getAllFluxesOfTeam(team.getId()),
			servicePicker.getScreenService().getAllScreens(),
			servicePicker.getScreenService().getAllScreensOfTeam(team.getId()),
			servicePicker.getUserService().getAllUsers(),
			servicePicker.getUserService().getAllMembersOfTeam(team.getId()),
			servicePicker.getUserService().getAllAdminsOfTeam(team.getId()),
			servicePicker.getScheduleService().getAllSchedules(),
			servicePicker.getScheduleService().getAllSchedulesOfTeam(team.getId()),
			servicePicker.getDiffuserService().getAllDiffusers(),
			servicePicker.getDiffuserService().getAllDiffusersOfTeam(team.getId()),
			new TeamData(team),
			error));
	}

	// @With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<TeamData> boundForm = form.bindFromRequest(request);
		TeamService teamService = servicePicker.getTeamService();
		TeamData data = boundForm.get();

		if (teamService.getTeamByName(data.getName()) != null) {
			return createViewWithErrorMessage("Team name already exists");
		}
		else {
			Team team = new Team(data.getName());

			Result error = checkDataIntegrity(data, "createFromFluxLoop");
			if (error != null) {
				return error;
			}

			fillTeamFromTeamData(team, data);

			teamService.create(team);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<TeamData> boundForm = form.bindFromRequest(request);

		TeamData data = boundForm.get();
		TeamService teamService = servicePicker.getTeamService();
		Team team = teamService.getTeamByName(data.getName());

		if (team == null) {
			return updateViewWithErrorMessage(data.getName(), "Team name does not exists");
		}
		else {

			Result error = checkDataIntegrity(data, "update");
			if (error != null) {
				return error;
			}

			fillTeamFromTeamData(team, data);

			teamService.update(team);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String name) {
		TeamService teamService = servicePicker.getTeamService();
		Team team = teamService.getTeamByName(name);

		if (team == null) {
			// team does not exists
			return indexWithErrorMessage("Team name does not exists");
		}
		else {
			teamService.delete(team);
			return index();
		}
	}

	private void fillTeamFromTeamData(Team team, TeamData data) {

		for (String fluxName: data.getFluxes()) {
			team.addToFluxes(servicePicker.getFluxService().getFluxByName(fluxName).getId());
		}

		for (String email: data.getMembers()) {
			team.addMember(servicePicker.getUserService().getMemberByUserEmail(email).getUserId());
		}

		for (String email: data.getAdmins()) {
			team.addAdmin(servicePicker.getUserService().getMemberByUserEmail(email).getUserId());
		}

		for (String scheduleName: data.getSchedules()) {
			team.addToSchedules(servicePicker.getScheduleService().getScheduleByName(scheduleName).getId());
		}

		for (String diffuserName: data.getDiffusers()) {
			team.addToDiffusers(servicePicker.getDiffuserService().getDiffuserByName(diffuserName).getId());
		}

		for (String screenMAC: data.getScreens()) {
			team.addScreen(servicePicker.getScreenService().getScreenByMacAddress(screenMAC).getId());
		}
	}


	private Result checkDataIntegrity(TeamData data, String action) {

		Result error = null;

		if (data.getFluxes() != null) {
			for (String fluxName: data.getFluxes()) {
				if (servicePicker.getFluxService().getFluxByName(fluxName) == null) {
					if (action.equals("createFromFluxLoop"))
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
				if (servicePicker.getUserService().getMemberByUserEmail(email) == null) {
					if (action.equals("createFromFluxLoop"))
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
				if (servicePicker.getUserService().getMemberByUserEmail(email) == null) {
					if (action.equals("createFromFluxLoop"))
						error = createViewWithErrorMessage("Admin email does not exists");
					else if (action.equals("update"))
						error = updateViewWithErrorMessage(data.getName(), "Admin email does not exists");
				}
			}
		}
		else {
			data.setAdmins(new ArrayList<>());
		}

		if (data.getSchedules() != null) {
			for (String scheduleName: data.getSchedules()) {
				if (servicePicker.getScheduleService().getScheduleByName(scheduleName) == null) {
					if (action.equals("createFromFluxLoop"))
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
				if (servicePicker.getDiffuserService().getDiffuserByName(diffuserName) == null) {
					if (action.equals("createFromFluxLoop"))
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
				if (servicePicker.getScreenService().getScreenByMacAddress(screenMAC) == null) {
					if (action.equals("createFromFluxLoop"))
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
