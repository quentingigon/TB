package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;
import models.db.Team;
import models.entities.DataUtils;
import models.entities.FluxData;
import models.repositories.interfaces.FluxRepository;
import models.repositories.interfaces.SiteRepository;
import models.repositories.interfaces.TeamRepository;
import play.api.Play;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Files;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.flux.flux_creation;
import views.html.flux.flux_page;
import views.html.flux.flux_update;

import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import static java.nio.file.Files.readAllBytes;
import static services.BlockUtils.blockNumber;

public class FluxController extends Controller {

	private final String PICTURES_DIR = System.getProperty("user.dir") + "/public/images/";

	@Inject
	FluxRepository fluxRepository;

	@Inject
	SiteRepository siteRepository;

	@Inject
	TeamRepository teamRepository;

	private Form<FluxData> form;

	private DataUtils dataUtils;

	@Inject
	public FluxController(FormFactory formFactory, DataUtils dataUtils) {
		this.dataUtils = dataUtils;
		this.form = formFactory.form(FluxData.class);
	}

	@With(UserAuthentificationAction.class)
	public Result index() {
		return ok(flux_page.render(dataUtils.getAllFluxes(), null));
	}

	@With(UserAuthentificationAction.class)
	public Result indexWithErrorMessage(String error) {
		return badRequest(flux_page.render(dataUtils.getAllFluxes(), error));
	}

	@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(flux_creation.render(form, null));
	}

	@With(UserAuthentificationAction.class)
	public Result createViewWithErrorMessage(String error) {
		return badRequest(flux_creation.render(form, error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String name) {
		return ok(flux_update.render(form, new FluxData(fluxRepository.getByName(name)), null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateViewWithMessage(String name, String error) {
		return badRequest(flux_update.render(form, new FluxData(fluxRepository.getByName(name)), error));
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<FluxData> boundForm = form.bindFromRequest(request);
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());

		FluxData data = boundForm.get();

		// flux already exists
		if (fluxRepository.getByName(data.getName()) != null) {
			return createViewWithErrorMessage("Flux already exists");
		}
		// bar url
		else if (data.getType().equals("URL") && !isValidURL(data.getUrl())) {
			return createViewWithErrorMessage("URL format is wrong");
		}
		// duration not a number
		else if (!isInteger(data.getDuration())) {
			return createViewWithErrorMessage("You must enter an integer for duration");
		}
		// flux duration is too long
		else if (Integer.valueOf(data.getDuration()) > blockNumber) {
			return createViewWithErrorMessage("Flux duration is too long");
		}
		else {

			Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
			Http.MultipartFormData.FilePart<Files.TemporaryFile> picture = body.getFile("picture");

			// copy file to server files
			if (picture != null) {
				String fileName = picture.getFilename();
				Files.TemporaryFile file = picture.getRef();
				String pictureUrl = PICTURES_DIR + fileName;
				file.copyTo(Paths.get(pictureUrl), true);

				//data.setUrl(file.path().toString());
				data.setUrl("/assets/images/" + fileName);
			}

			Flux newFlux = new Flux(data);

			newFlux = fluxRepository.addFlux(newFlux);

			// general flux
			if (data.getSite() != null && data.getSite().equals("")) {
				fluxRepository.addGeneralFlux(new GeneralFlux(newFlux.getId()));

			}
			// located flux
			else {
				fluxRepository.addLocatedFlux(new LocatedFlux(newFlux.getId(),
					siteRepository.getByName(data.getSite().toLowerCase()).getId()));
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
			return updateViewWithMessage(data.getName(), "Flux name does not exists");
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
			return indexWithErrorMessage("Flux does not exists");
		}
		else {
			fluxRepository.delete(flux);
			return index();
		}
	}

	private boolean isValidURL(String urlStr) {
		try {
			new URL(urlStr);
			return true;
		}
		catch (MalformedURLException e) {
			return false;
		}
	}

	private boolean isInteger( String input ) {
		try {
			Integer.parseInt( input );
			return true;
		}
		catch( Exception e ) {
			return false;
		}
	}
}
