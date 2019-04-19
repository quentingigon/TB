package controllers;

import models.db.Screen;
import models.db.WaitingScreen;
import models.repositories.ScreenRepository;
import models.repositories.WaitingScreenRepository;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.eventsource;
import views.html.screen_code;
import views.html.screen_register;

import javax.inject.Inject;
import java.util.UUID;

public class ScreenController extends Controller {

	@Inject
	ScreenRepository screenRepository;

	@Inject
	WaitingScreenRepository waitingScreenRepository;

	@Inject
	private FormFactory formFactory;

	public Result index() {
		return ok(screen_register.render(formFactory.form(String.class)));
	}

	public Result authentification(Http.Request request) {

		String macAdr = request.queryString().get("mac")[0];

		Screen screen = screenRepository.getByMacAddress(macAdr);

		// screen not registered
		if (screen == null) {
			String code = screenRegisterCodeGenerator();

			waitingScreenRepository.add(new WaitingScreen(macAdr, code));

			return ok(screen_code.render(code));
		}
		// screen registered
		else {

			// screen already logged in
			if (screen.isLogged()) {
				return ok(eventsource.render());
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

	public Result register(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		String macAdr = boundForm.get("mac");

		// screen is already known
		if (screenRepository.getByMacAddress(macAdr) != null) {
			// TODO: return error (with error handling)
			return index();
		}
		else {
			Screen newScreen = new Screen(macAdr);
			String code = boundForm.get("code");

			// if code is correct
			if (waitingScreenRepository.getByMac(macAdr).getCode().equals(code)) {
				screenRepository.add(newScreen);

				return redirect(routes.HomeController.index());
			}
			else {
				// wrong code
				return index();
			}
		}
	}

	public Result updateScreen(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		Screen screen = screenRepository.getByMacAddress(boundForm.get("mac"));

		if (screen == null) {
			// screen is not known
			// TODO: return error (with error handling)
			return index();
		}
		else {
			// TODO modify screen here
			return redirect(routes.HomeController.index());
		}
	}

	private String screenRegisterCodeGenerator() {
		UUID uniqueKey = UUID.randomUUID();

		return uniqueKey.toString().substring(0, 5);
	}
}
