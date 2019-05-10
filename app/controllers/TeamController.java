package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.*;
import models.repositories.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.schedule_creation;
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

	//@With(UserAuthentificationAction.class)
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

	//@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(team_creation.render(form, getAllScreens(), null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String teamName) {
		Team team = teamRepository.getByName(teamName);
		return ok(team_update.render(form,
			getAllFluxes(),
			getAllScreens(),
			getAllMembers(),
			getAllSchedules(),
			getAllDiffusers(),
			new TeamData(team),
			null));
	}

	// @With(UserAuthentificationAction.class)
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
				getAllFluxes(),
				getAllScreens(),
				getAllMembers(),
				getAllSchedules(),
				getAllDiffusers(),
				new TeamData(data.getName()),
				"Team name does not exists"));
		}
		else {
			if (data.getFluxes() != null) {
				for (String fluxName: data.getFluxes()) {
					if (fluxRepository.getByName(fluxName) == null) {
						return badRequest(team_update.render(form,
							getAllFluxes(),
							getAllScreens(),
							getAllMembers(),
							getAllSchedules(),
							getAllDiffusers(),
							new TeamData(data.getName()),
							"Flux name does not exists"));
					}

					team.addToFluxes(fluxRepository.getByName(fluxName).getId());
				}
			}
			if (data.getMembers() != null) {
				for (String email: data.getMembers()) {
					if (userRepository.getByEmail(email) == null) {
						return badRequest(team_update.render(form,
							getAllFluxes(),
							getAllScreens(),
							getAllMembers(),
							getAllSchedules(),
							getAllDiffusers(),
							new TeamData(data.getName()),
							"User email address does not exists"));
					}

					team.addMember(userRepository.getByEmail(email).getId());
				}
			}
			if (data.getSchedules() != null) {
				for (String scheduleName: data.getSchedules()) {
					if (scheduleRepository.getByName(scheduleName) == null) {
						return badRequest(team_update.render(form,
							getAllFluxes(),
							getAllScreens(),
							getAllMembers(),
							getAllSchedules(),
							getAllDiffusers(),
							new TeamData(data.getName()),
							"Schedule name does not exists"));
					}

					team.addToSchedules(scheduleRepository.getByName(scheduleName).getId());
				}
			}
			if (data.getDiffusers() != null) {
				for (String diffuserName: data.getDiffusers()) {
					if (diffuserRepository.getByName(diffuserName) == null) {
						return badRequest(team_update.render(form,
							getAllFluxes(),
							getAllScreens(),
							getAllMembers(),
							getAllSchedules(),
							getAllDiffusers(),
							new TeamData(data.getName()),
							"Diffuser name does not exists"));
					}

					team.addToDiffusers(diffuserRepository.getByName(diffuserName).getId());
				}
			}

			if (data.getScreens() != null) {
				for (String screenMAC: data.getScreens()) {
					if (screenRepository.getByMacAddress(screenMAC) == null) {
						return badRequest(team_update.render(form,
							getAllFluxes(),
							getAllScreens(),
							getAllMembers(),
							getAllSchedules(),
							getAllDiffusers(),
							new TeamData(data.getName()),
							"Diffuser name does not exists"));
					}

					team.addScreen(screenRepository.getByMacAddress(screenMAC).getId());
				}
			}

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

	private List<ScreenData> getAllScreens() {
		List<ScreenData> data = new ArrayList<>();
		for (Screen s: screenRepository.getAll()) {
			data.add(new ScreenData(s));
		}
		return data;
	}

	private List<FluxData> getAllFluxes() {
		List<FluxData> data = new ArrayList<>();
		for (Flux f: fluxRepository.getAll()) {
			data.add(new FluxData(f));
		}
		return data;
	}

	private List<ScheduleData> getAllSchedules() {
		List<ScheduleData> data = new ArrayList<>();
		for (Schedule s: scheduleRepository.getAll()) {
			data.add(new ScheduleData(s));
		}
		return data;
	}

	private List<DiffuserData> getAllDiffusers() {
		List<DiffuserData> data = new ArrayList<>();
		for (Diffuser d: diffuserRepository.getAll()) {
			data.add(new DiffuserData(d));
		}
		return data;
	}

	private List<UserData> getAllMembers() {
		List<UserData> data = new ArrayList<>();
		for (User u: userRepository.getAll()) {
			data.add(new UserData(u));
		}
		return data;
	}
}
