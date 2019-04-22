package controllers;

import models.db.Team;
import models.entities.TeamData;
import models.repositories.TeamRepository;
import play.data.Form;
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

	private Form<TeamData> form;

	@Inject
	public TeamController(FormFactory formFactory) {
		this.form = formFactory.form(TeamData.class);
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<TeamData> boundForm = form.bindFromRequest(request);

		if (teamRepository.getByName(boundForm.get().getName()) != null) {
			// team already exists
			// TODO change
			return redirect(routes.HomeController.index());
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
