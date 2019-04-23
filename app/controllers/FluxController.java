package controllers;

import models.db.Flux;
import models.entities.FluxData;
import models.repositories.FluxRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.flux_creation;

import javax.inject.Inject;

public class FluxController extends Controller {

	@Inject
	FluxRepository fluxRepository;

	private Form<FluxData> form;

	@Inject
	public FluxController(FormFactory formFactory) {
		this.form = formFactory.form(FluxData.class);
	}

	public Result createView() {
		return ok(flux_creation.render(form));
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<FluxData> boundForm = form.bindFromRequest(request);

		Flux newFlux = new Flux(boundForm.get().getName(), boundForm.get().getUrl());

		// flux already exists
		if (fluxRepository.getByName(boundForm.get().getName()) != null) {
			// TODO error + correct redirect
			return redirect(routes.HomeController.index());
		}
		else {

			fluxRepository.add(newFlux);
			return redirect(routes.HomeController.index());
		}
	}

	public Result update(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<FluxData> boundForm = form.bindFromRequest(request);

		Flux flux = fluxRepository.getByName(boundForm.get().getName());

		// flux does not exist
		if (fluxRepository.getByName(boundForm.get().getName()) == null) {
			// TODO error + correct redirect
			return redirect(routes.HomeController.index());
		}
		else {

			fluxRepository.update(flux);
			return redirect(routes.HomeController.index());
		}
	}
}
