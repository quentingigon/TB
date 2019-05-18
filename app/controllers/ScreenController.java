package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.Screen;
import models.db.Team;
import models.db.WaitingScreen;
import models.entities.DataUtils;
import models.entities.ScreenData;
import models.repositories.ScreenRepository;
import models.repositories.SiteRepository;
import models.repositories.TeamRepository;
import models.repositories.WaitingScreenRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.eventsource;
import views.html.screen.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ScreenController extends Controller {

	@Inject
	SiteRepository siteRepository;

	@Inject
	ScreenRepository screenRepository;

	@Inject
	TeamRepository teamRepository;

	@Inject
	WaitingScreenRepository waitingScreenRepository;

	private Form<ScreenData> form;

	private DataUtils dataUtils;

	@Inject
	public ScreenController(FormFactory formFactory, DataUtils dataUtils) {
		this.form = formFactory.form(ScreenData.class);
		this.dataUtils = dataUtils;
	}

	@With(UserAuthentificationAction.class)
	public Result index(Http.Request request) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(screen_page.render(dataUtils.getAllScreensOfTeam(teamId), null));
	}

	public Result indexWithErrorMessage(Http.Request request, String error) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(screen_page.render(dataUtils.getAllScreensOfTeam(teamId), error));
	}

	@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(screen_creation.render(form, null));
	}

	public Result createViewWithErrorMessage(String error) {
		return badRequest(screen_creation.render(form, error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String mac) {
		return ok(screen_update.render(form, new ScreenData(screenRepository.getByMacAddress(mac)), null));
	}

	public Result updateViewWithErrorMessage(String mac, String error) {
		return badRequest(screen_update.render(form, new ScreenData(screenRepository.getByMacAddress(mac)), error));
	}

	public Result authentification(Http.Request request) {

		String macAdr = request.queryString().get("mac")[0];

		Screen screen = screenRepository.getByMacAddress(macAdr);

		// screen not registered
		if (screen == null) {

			// if screen already asked for a code
			if (waitingScreenRepository.getByMac(macAdr) != null) {
				return ok(screen_code.render(waitingScreenRepository.getByMac(macAdr).getCode()));
			}

			String code = screenRegisterCodeGenerator();

			waitingScreenRepository.add(new WaitingScreen(code, macAdr));

			// send code
			return ok(screen_code.render(code));
		}
		// screen registered
		else {

			// screen already logged in
			if (screen.isLogged()) {
				return ok(eventsource.render()).withCookies(
					Http.Cookie.builder("mac", macAdr)
						.withHttpOnly(false)
						.build());
			}
			else {
				screen.setLogged(true);

				screenRepository.update(screen);

				return ok(eventsource.render()).withCookies(
					Http.Cookie.builder("mac", macAdr)
						.withHttpOnly(false)
						.build());
			}
		}
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<ScreenData> boundForm = form.bindFromRequest(request);
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());

		ScreenData data = boundForm.get();
		String macAdr = data.getMac();

		// screen is already known
		if (screenRepository.getByMacAddress(macAdr) != null) {
			return createViewWithErrorMessage("Screen already exists");
		}
		else if (data.getCode() == null) {
			return createViewWithErrorMessage("You must enter a registration code");
		}
		else {
			String code = data.getCode();
			WaitingScreen ws = waitingScreenRepository.getByMac(macAdr);

			if (ws == null) {
				return createViewWithErrorMessage(
					"You must first get a registration code by going to this address: /auth?mac=<YourMacAddress>");
			}

			// if code is correct -> add screen to DB
			if (ws.getCode().equals(code)) {

				Screen newScreen = new Screen(macAdr);

				if (siteRepository.getByName(data.getSite().toLowerCase()) == null) {
					return createViewWithErrorMessage("Bad site name");
				}
				newScreen.setSiteId(siteRepository.getByName(data.getSite().toLowerCase()).getId());
				newScreen.setResolution(data.getResolution());
				newScreen.setLogged(false);
				newScreen.setName(data.getName());

				screenRepository.add(newScreen);
				waitingScreenRepository.delete(ws);

				// add new schedule to current user's team
				Team team = teamRepository.getById(teamId);
				team.addScreen(newScreen.getId());
				teamRepository.update(team);

				return index(request);
			}
			else {
				// wrong code
				return createView();
			}
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<ScreenData> boundForm = form.bindFromRequest(request);

		ScreenData data = boundForm.get();

		Screen screen = screenRepository.getByMacAddress(data.getMac());

		if (screen == null) {
			// screen is not known
			return updateViewWithErrorMessage(data.getMac(), "MAC address does not exists");
		}
		else {
			// update screen
			if (siteRepository.getByName(data.getSite().toLowerCase()) == null) {
				return updateViewWithErrorMessage(data.getMac(), "Bad site name");
			}
			screen.setSiteId(siteRepository.getByName(data.getSite().toLowerCase()).getId());

			screen.setName(data.getName());
			screen.setMacAddress(data.getMac());
			screen.setResolution(data.getResolution());

			screenRepository.update(screen);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(Http.Request request, String mac) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		Screen screen = screenRepository.getByMacAddress(mac);

		if (screen == null) {
			// screen is not known
			return badRequest(screen_page.render(dataUtils.getAllScreensOfTeam(teamId), "MAC address does not exists"));
		}
		else {
			// TODO triggers to delete entry in team_screens
			screenRepository.delete(screen);
			return index(request);
		}
	}

	private String screenRegisterCodeGenerator() {
		UUID uniqueKey = UUID.randomUUID();

		return uniqueKey.toString().substring(0, 5);
	}
}
