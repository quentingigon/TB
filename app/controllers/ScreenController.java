package controllers;

import models.db.Screen;
import models.db.WaitingScreen;
import models.entities.ScreenData;
import models.repositories.ScreenRepository;
import models.repositories.SiteRepository;
import models.repositories.WaitingScreenRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.*;

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
	WaitingScreenRepository waitingScreenRepository;

	private Form<ScreenData> form;

	@Inject
	public ScreenController(FormFactory formFactory) {
		this.form = formFactory.form(ScreenData.class);
	}

	@With(UserAuthentificationAction.class)
	public Result index() {

		return (ok(screen_page.render(getAllScreens(), null)));
	}

	@With(UserAuthentificationAction.class)
	public Result registerView() {
		return ok(screen_register.render(form, null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String mac) {
		return ok(screen_update.render(form, new ScreenData(screenRepository.getByMacAddress(mac)), null));
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
	public Result register(Http.Request request) {
		//final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<ScreenData> boundForm = form.bindFromRequest(request);

		ScreenData data = boundForm.get();
		String macAdr = data.getMac();

		// screen is already known
		if (screenRepository.getByMacAddress(macAdr) != null) {
			return badRequest(screen_register.render(form, "Screen already exists, by MAC"));
		}
		else if (data.getCode() == null) {
			return badRequest(screen_register.render(form, "You must enter a registration code"));
		}
		else {
			String code = data.getCode();
			WaitingScreen ws = waitingScreenRepository.getByMac(macAdr);

			if (ws == null) {
				return badRequest(screen_register.render(form,
					"You must first get a registration code by going to this address: /auth?mac=<YOurMacAddress>"));
			}

			// if code is correct -> add screen to DB
			if (ws.getCode().equals(code)) {

				Screen newScreen = new Screen(macAdr);

				if (siteRepository.getByName(data.getSite().toLowerCase()) == null) {
					return badRequest(screen_register.render(form, "Bad site name"));
				}
				newScreen.setSiteId(siteRepository.getByName(data.getSite().toLowerCase()).getId());
				newScreen.setResolution(data.getResolution());
				newScreen.setLogged(false);
				newScreen.setName(data.getName());

				screenRepository.add(newScreen);
				// waitingScreenRepository.delete(ws);

				return index();
			}
			else {
				// wrong code
				return registerView();
			}
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<ScreenData> boundForm = form.bindFromRequest(request);

		ScreenData data = boundForm.get();

		Screen screen = screenRepository.getByMacAddress(data.getMac());

		if (screen == null) {
			// screen is not known
			return badRequest(screen_update.render(form, null, "MAC address already exists"));
		}
		else {
			// update screen
			if (siteRepository.getByName(data.getSite().toLowerCase()) == null) {
				return badRequest(screen_register.render(form, "Bad site name"));
			}
			screen.setSiteId(siteRepository.getByName(data.getSite().toLowerCase()).getId());

			screen.setName(data.getName());
			screen.setMacAddress(data.getMac());
			screen.setResolution(data.getResolution());

			screenRepository.update(screen);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String mac) {

		Screen screen = screenRepository.getByMacAddress(mac);

		if (screen == null) {
			// screen is not known
			return badRequest(screen_page.render(getAllScreens(), "MAC address does not exists"));
		}
		else {
			screenRepository.delete(screen);
			return index();
		}
	}

	private String screenRegisterCodeGenerator() {
		UUID uniqueKey = UUID.randomUUID();

		return uniqueKey.toString().substring(0, 5);
	}

	private List<ScreenData> getAllScreens() {
		List<ScreenData> data = new ArrayList<>();
		for (Screen s: screenRepository.getAll()) {
			data.add(new ScreenData(s));
		}
		return data;
	}
}
