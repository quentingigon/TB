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

		Team newTeam = teamRepository.getByName(boundForm.get("name"));

		if (newTeam != null) {
			// team already exists
			// TODO change
			return redirect(routes.HomeController.index());
		}
		else {
			teamRepository.add(newTeam);
			return redirect(routes.HomeController.index());
		}
	}
}
