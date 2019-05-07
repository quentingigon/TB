package controllers;

import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;
import models.entities.FluxData;
import models.repositories.FluxRepository;
import models.repositories.SiteRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.flux_creation;
import views.html.flux_page;
import views.html.flux_update;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static services.BlockUtils.blockNumber;

public class FluxController extends Controller {

	@Inject
	FluxRepository fluxRepository;

	@Inject
	SiteRepository siteRepository;

	private Form<FluxData> form;

	@Inject
	public FluxController(FormFactory formFactory) {
		this.form = formFactory.form(FluxData.class);
	}

	public Result index() {
		return ok(flux_page.render(getAllFluxes(), null));
	}

	public Result createView() {
		return ok(flux_creation.render(form, null));
	}

	public Result updateView(String name) {
		return ok(flux_update.render(form, new FluxData(fluxRepository.getByName(name)), null));
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<FluxData> boundForm = form.bindFromRequest(request);

		FluxData data = boundForm.get();

		Flux newFlux = new Flux(data);

		// flux already exists
		if (fluxRepository.getByName(data.getName()) != null) {
			return badRequest(flux_creation.render(form, "Flux already exists"));
		}
		// bar url
		else if (!isValidURL(data.getUrl())) {
			return badRequest(flux_creation.render(form, "URL format is wrong"));
		}
		// duration not a number
		else if (data.getDuration().matches("-?\\d+(\\.\\d+)?")) {
			return badRequest(flux_creation.render(form, "You must enter a number for duration"));
		}
		else if (Integer.valueOf(data.getDuration()) > blockNumber) {
			return badRequest(flux_creation.render(form, "Flux duration is too long"));
		}
		else {

			// general flux
			if (data.getSite() == null) {
				newFlux = fluxRepository.addFlux(newFlux);
				fluxRepository.addGeneralFlux(new GeneralFlux(newFlux.getId()));

			}
			// located flux
			else {
				newFlux = fluxRepository.addFlux(newFlux);
				fluxRepository.addLocatedFlux(new LocatedFlux(newFlux.getId(),
					siteRepository.getByName(data.getSite()).getId()));
			}


			return index();
		}
	}

	public Result update(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<FluxData> boundForm = form.bindFromRequest(request);

		FluxData data = boundForm.get();

		Flux flux = fluxRepository.getByName(data.getName());

		// flux does not exist
		if (flux == null) {
			return badRequest(flux_creation.render(form, "Flux name does not exists"));
		}
		// update flux
		else {
			flux.setDuration(Long.valueOf(data.getDuration()));
			flux.setUrl(data.getUrl());
			flux.setType(data.getType());
			fluxRepository.update(flux);

			return index();
		}
	}

	public Result delete(String name) {
		Flux flux = fluxRepository.getByName(name);

		if (flux == null) {
			// team does not exists
			return badRequest(flux_page.render(null, "Flux name does not exists"));
		}
		else {
			fluxRepository.delete(flux);
			return index();
		}
	}

	private List<FluxData> getAllFluxes() {
		List<FluxData> data = new ArrayList<>();
		for (Flux f: fluxRepository.getAll()) {
			data.add(new FluxData(f));
		}
		return data;
	}

	private boolean isValidURL(String urlStr) {
		try {
			URL url = new URL(urlStr);
			return true;
		}
		catch (MalformedURLException e) {
			return false;
		}
	}
}
