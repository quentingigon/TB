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
import views.html.team_creation;

import javax.inject.Inject;

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

	public Result createView() {
		return ok(team_creation.render(form, screenRepository.getAll()));
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<TeamData> boundForm = form.bindFromRequest(request);

		if (teamRepository.getByName(boundForm.get().getName()) != null) {
			// team already exists
			// TODO error
			return createView();
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
			// TODO error
			return redirect(routes.HomeController.index());
		}
		else {
			// TODO update with values from form
			teamRepository.update(team);

			return redirect(routes.HomeController.index());
		}

	}
}
