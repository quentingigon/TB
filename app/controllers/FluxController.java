package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;
import models.db.Team;
import models.entities.FluxData;
import models.repositories.FluxRepository;
import models.repositories.SiteRepository;
import models.repositories.TeamRepository;
import models.repositories.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.flux.flux_creation;
import views.html.flux.flux_page;
import views.html.flux.flux_update;

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

	@Inject
	UserRepository userRepository;

	@Inject
	TeamRepository teamRepository;

	private Form<FluxData> form;

	@Inject
	public FluxController(FormFactory formFactory) {
		this.form = formFactory.form(FluxData.class);
	}

	@With(UserAuthentificationAction.class)
	public Result index() {
		return ok(flux_page.render(getAllFluxes(), null));
	}

	@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(flux_creation.render(form, null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String name) {
		return ok(flux_update.render(form, new FluxData(fluxRepository.getByName(name)), null));
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<FluxData> boundForm = form.bindFromRequest(request);
		Integer teamId = getTeamIdOfUserByEmail(request.cookie("email").value());

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
		// flux duration is too long
		else if (Integer.valueOf(data.getDuration()) > blockNumber) {
			return badRequest(flux_creation.render(form, "Flux duration is too long"));
		}
		else {

			newFlux = fluxRepository.addFlux(newFlux);

			// general flux
			if (data.getSite() == null) {
				fluxRepository.addGeneralFlux(new GeneralFlux(newFlux.getId()));

			}
			// located flux
			else {
				fluxRepository.addLocatedFlux(new LocatedFlux(newFlux.getId(),
					siteRepository.getByName(data.getSite()).getId()));
			}

			if (teamId != null) {
				Team team = teamRepository.getById(teamId);
				team.addToFluxes(newFlux.getId());
				teamRepository.update(team);
			}

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
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

	@With(UserAuthentificationAction.class)
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

	private Integer getTeamIdOfUserByEmail(String email) {
		return userRepository
			.getMemberByUserEmail(email)
			.getTeamId();
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
