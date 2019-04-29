package controllers;

import models.db.Team;
import models.entities.TeamData;
import models.repositories.ScreenRepository;
import models.repositories.TeamRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.flux_creation;
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

	private Form<TeamData> form;

	@Inject
	public TeamController(FormFactory formFactory) {
		this.form = formFactory.form(TeamData.class);
	}

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

	public Result createView() {
		return ok(team_creation.render(form, null));
	}

	public Result updateView(String teamName) {
		return ok(team_update.render(form, new TeamData(teamRepository.getByName(teamName)), null));
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<TeamData> boundForm = form.bindFromRequest(request);

		if (teamRepository.getByName(boundForm.get().getName()) != null) {
			// team already exists
			return badRequest(team_creation.render(form, "Team name already exists"));
		}
		else {
			Team newTeam = new Team(boundForm.get().getName());

			teamRepository.add(newTeam);

			return redirect(routes.HomeController.index());
		}
	}

	public Result update(Http.Request request) {
		//final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<TeamData> boundForm = form.bindFromRequest(request);

		Team team = teamRepository.getByName(boundForm.get().getName());

		if (team == null) {
			// team does not exists
			return badRequest(team_update.render(form, new TeamData(boundForm.get().getName()), "Team name does not exists"));
		}
		else {
			// TODO update with values from form
			teamRepository.update(team);

			return redirect(routes.HomeController.index());
		}
	}

	public Result delete(Http.Request request, String name) {
		Team team = teamRepository.getByName(name);

		if (team == null) {
			// team does not exists
			return badRequest(team_page.render(null, "Team name does not exists"));
		}
		else {
			teamRepository.delete(team);
			return redirect(routes.HomeController.index());
		}
	}
}
