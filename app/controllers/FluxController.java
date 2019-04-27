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
import views.html.flux_page;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class FluxController extends Controller {

	@Inject
	FluxRepository fluxRepository;

	private Form<FluxData> form;

	@Inject
	public FluxController(FormFactory formFactory) {
		this.form = formFactory.form(FluxData.class);
	}

	public Result index() {
		/*
		List<String> row1 = Arrays.asList("f1", "heig-vd.ch", "10000", "url");
		List<String> row2 = Arrays.asList("f2", "heig-vd.ch", "8000", "url");

		List<List<String>> data = new ArrayList<>();
		data.add(row1);
		data.add(row2);*/

		List<FluxData> data2 = new ArrayList<>();
		data2.add(new FluxData("f1", "heig-vd.ch", "10000", "url"));
		data2.add(new FluxData("f2", "heig-vd.ch", "8000", "url"));

		/*
		var strings = Stream.of(new String[][] {
			{"f1", "https://heig-vd.ch", "10000", "url"},
			{"f2", "https://heig-vd.ch", "8000", "url"}
		})
			.map(Arrays::asList)
			.collect(Collectors.toList());
*/
		return ok(flux_page.render(data2, null));
	}

	public Result createView() {
		return ok(flux_creation.render(form, null));
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<FluxData> boundForm = form.bindFromRequest(request);

		Flux newFlux = new Flux(boundForm.get().getName(), boundForm.get().getUrl());

		// flux already exists
		if (fluxRepository.getByName(boundForm.get().getName()) != null) {
			// with error message
			return badRequest(flux_creation.render(form, "Flux already exists"));
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
			return status(440, "Flux name does not exist");
		}
		else {

			fluxRepository.update(flux);
			return redirect(routes.HomeController.index());
		}
	}
}
