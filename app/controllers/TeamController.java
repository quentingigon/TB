package controllers;

import models.db.Team;
import models.repositories.TeamRepository;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

public class TeamController extends Controller {

	@Inject
	private FormFactory formFactory;

	@Inject
	TeamRepository teamRepository;

	public Result create(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		if (teamRepository.getByName(boundForm.get("name")) != null) {
			// team already exists
			// TODO change
			return redirect(routes.HomeController.index());
		}
		else {
			Team newTeam = new Team(boundForm.get("name"));

			teamRepository.add(newTeam);

			return redirect(routes.HomeController.index());
		}
	}

	public Result update(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		Team team = teamRepository.getByName(boundForm.get("name"));

		if (team == null) {
			// team does not exists
			// TODO change
			return redirect(routes.HomeController.index());
		}
		else {
			// TODO update with values from form
			teamRepository.update(team);

			return redirect(routes.HomeController.index());
		}

	}
}
