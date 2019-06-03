package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.Flux;
import models.db.GeneralFlux;
import models.db.LocatedFlux;
import models.db.Team;
import models.entities.FluxData;
import models.repositories.interfaces.SiteRepository;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Files;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.FluxService;
import services.ServicePicker;
import services.TeamService;
import views.html.flux.flux_creation;
import views.html.flux.flux_page;
import views.html.flux.flux_update;

import javax.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import static services.BlockUtils.blockNumber;

/**
 * This class implements a controller for the Fluxes.
 * It gives CRUD operations.
 */
public class FluxController extends Controller {

	private final String PICTURES_DIR = System.getProperty("user.dir") + "/public/images/";

	@Inject
	SiteRepository siteRepository;

	private Form<FluxData> form;

	private final ServicePicker servicePicker;

	@Inject
	public FluxController(FormFactory formFactory,
						  ServicePicker servicePicker) {
		this.servicePicker = servicePicker;
		this.form = formFactory.form(FluxData.class);
	}

	@With(UserAuthentificationAction.class)
	public Result index() {
		return ok(flux_page.render(servicePicker.getFluxService().getAllFluxes(),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result indexWithErrorMessage(String error) {
		return badRequest(flux_page.render(servicePicker.getFluxService().getAllFluxes(),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(flux_creation.render(form,
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result createViewWithErrorMessage(String error) {
		return badRequest(flux_creation.render(form,
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String name) {
		return ok(flux_update.render(form,
			new FluxData(servicePicker.getFluxService().getFluxByName(name)),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateViewWithErrorMessage(String name, String error) {
		return badRequest(flux_update.render(form,
			new FluxData(servicePicker.getFluxService().getFluxByName(name)),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<FluxData> boundForm = form.bindFromRequest(request);
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		FluxService fluxService = servicePicker.getFluxService();
		TeamService teamService = servicePicker.getTeamService();
		FluxData data = boundForm.get();

		// flux already exists
		if (fluxService.getFluxByName(data.getName()) != null) {
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
		else if (Integer.parseInt(data.getDuration()) > blockNumber) {
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

				data.setUrl("/assets/images/" + fileName);
			}

			Flux newFlux = new Flux(data);

			newFlux = fluxService.create(newFlux);

			// general flux
			if (data.getSite() != null && data.getSite().equals("")) {
				fluxService.createGeneral(new GeneralFlux(newFlux.getId()));

			}
			// located flux
			else {
				fluxService.createLocated(new LocatedFlux(newFlux.getId(),
					siteRepository.getByName(data.getSite().toLowerCase()).getId()));
			}

			if (teamId != null) {
				Team team = teamService.getTeamById(teamId);
				team.addToFluxes(newFlux.getId());

				teamService.update(team);
			}

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<FluxData> boundForm = form.bindFromRequest(request);
		FluxService fluxService = servicePicker.getFluxService();

		FluxData data = boundForm.get();

		Flux flux = fluxService.getFluxByName(data.getName());

		// flux does not exist
		if (flux == null) {
			return updateViewWithErrorMessage(data.getName(), "Flux name does not exists");
		}
		// update flux
		else {
			flux.setDuration(Long.parseLong(data.getDuration()));
			flux.setUrl(data.getUrl());
			flux.setType(data.getType());
			flux.setNumberOfPhases(Long.parseLong(data.getNumberOfPhases()));

			fluxService.update(flux);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String name) {
		FluxService fluxService = servicePicker.getFluxService();
		Flux flux = fluxService.getFluxByName(name);

		if (flux == null) {
			// team does not exists
			return indexWithErrorMessage("Flux does not exists");
		}
		else {
			fluxService.delete(flux);
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
