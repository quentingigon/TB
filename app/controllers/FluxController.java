package controllers;

import models.db.Flux;
import models.repositories.FluxRepository;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

public class FluxController extends Controller {

	@Inject
	FluxRepository fluxRepository;

	@Inject
	private FormFactory formFactory;

	public Result create(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		Flux newFlux = new Flux(boundForm.get("name"), boundForm.get("url"));

		// flux already exists
		if (fluxRepository.getByName(boundForm.get("name")) != null) {
			// TODO error + correct redirect
			return redirect(routes.HomeController.index());
		}
		else {

			fluxRepository.add(newFlux);
			return redirect(routes.HomeController.index());
		}
	}

	public Result update(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		Flux flux = fluxRepository.getByName(boundForm.get("name"));

		// flux does not exist
		if (fluxRepository.getByName(boundForm.get("name")) == null) {
			// TODO error + correct redirect
			return redirect(routes.HomeController.index());
		}
		else {

			fluxRepository.update(flux);
			return redirect(routes.HomeController.index());
		}
	}
}
