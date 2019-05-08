package controllers;

import models.db.Screen;
import models.db.Team;
import models.entities.*;
import models.repositories.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.team_creation;
import views.html.team_page;
import views.html.team_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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

	@Inject
	public TeamController(FormFactory formFactory) {
		this.form = formFactory.form(TeamData.class);
	}

	@With(UserAuthentificationAction.class)
	public Result index() {
		List<TeamData> data = new ArrayList<>();
		for (Team t: teamRepository.getAll()) {
			TeamData td = new TeamData(t);
			List<String> members = new ArrayList<>();
			members.add("user1");
			td.setMembers(members);
			data.add(td);
		}
		return ok(team_page.render(data, null));
	}

	@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(team_creation.render(form, getAllScreens(), null));
	}

	// TODO don't send only data of team
	@With(UserAuthentificationAction.class)
	public Result updateView(String teamName) {
		Team team = teamRepository.getByName(teamName);
		return ok(team_update.render(form,
			getAllFluxesOfTeam(team.getId()),
			getAllScreensOfTeam(team.getId()),
			getAllMembersOfTeam(team.getId()),
			getAllSchedulesOfTeam(team.getId()),
			getAllDiffusersOfTeam(team.getId()),
			new TeamData(team),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<TeamData> boundForm = form.bindFromRequest(request);

		TeamData data = boundForm.get();

		if (teamRepository.getByName(data.getName()) != null) {
			// team already exists
			return badRequest(team_creation.render(form, getAllScreens(), "Team name already exists"));
		}
		else {
			Team newTeam = new Team(data.getName());

			if (data.getScreens() != null) {
				for (String mac: data.getScreens()) {
					newTeam.addScreen(screenRepository.getByMacAddress(mac).getId());
				}
			}

			teamRepository.add(newTeam);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<TeamData> boundForm = form.bindFromRequest(request);

		TeamData data = boundForm.get();

		Team team = teamRepository.getByName(data.getName());

		if (team == null) {
			// team does not exists
			return badRequest(team_update.render(form,
				getAllFluxesOfTeam(team.getId()),
				getAllScreensOfTeam(team.getId()),
				getAllMembersOfTeam(team.getId()),
				getAllSchedulesOfTeam(team.getId()),
				getAllDiffusersOfTeam(team.getId()),
				new TeamData(data.getName()),
				"Team name does not exists"));
		}
		else {
			// TODO update with values from form
			teamRepository.update(team);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String name) {
		Team team = teamRepository.getByName(name);

		if (team == null) {
			// team does not exists
			return badRequest(team_page.render(null, "Team name does not exists"));
		}
		else {
			teamRepository.delete(team);
			return index();
		}
	}

	private List<ScreenData> getAllScreensOfTeam(int teamId) {
		List<ScreenData> data = new ArrayList<>();
		for (Integer screenId : screenRepository.getAllScreenIdsOfTeam(teamId)) {
			data.add(new ScreenData(screenRepository.getById(screenId)));
		}
		return data;
	}

	private List<FluxData> getAllFluxesOfTeam(int teamId) {
		List<FluxData> data = new ArrayList<>();
		for (Integer fluxId : fluxRepository.getAllFluxIdsOfTeam(teamId)) {
			data.add(new FluxData(fluxRepository.getById(fluxId)));
		}
		return data;
	}

	private List<UserData> getAllMembersOfTeam(int teamId) {
		List<UserData> data = new ArrayList<>();
		for (Integer userId : userRepository.getAllMemberIdsOfTeam(teamId)) {
			data.add(new UserData(userRepository.getById(userId)));
		}
		return data;
	}

	private List<ScheduleData> getAllSchedulesOfTeam(int teamId) {
		List<ScheduleData> data = new ArrayList<>();
		for (Integer scheduleId : scheduleRepository.getAllScheduleIdsOfTeam(teamId)) {
			data.add(new ScheduleData(scheduleRepository.getById(scheduleId)));
		}
		return data;
	}

	private List<DiffuserData> getAllDiffusersOfTeam(int teamId) {
		List<DiffuserData> data = new ArrayList<>();
		for (Integer diffuserId : diffuserRepository.getAllDiffuserIdsOfTeam(teamId)) {
			data.add(new DiffuserData(diffuserRepository.getById(diffuserId)));
		}
		return data;
	}

	private List<ScreenData> getAllScreens() {
		List<ScreenData> data = new ArrayList<>();
		for (Screen s: screenRepository.getAll()) {
			data.add(new ScreenData(s));
		}
		return data;
	}
}
