package controllers;

import models.db.Screen;
import models.repositories.ScreenRepository;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.eventsource;
import views.html.screen_code;
import views.html.screen_register;

import javax.inject.Inject;

public class ScreenController extends Controller {

	@Inject
	ScreenRepository screenRepository;

	@Inject
	private FormFactory formFactory;

	public Result index() {
		return ok(screen_register.render(formFactory.form(String.class)));
	}

	public Result authentification(Http.Request request) {

		String macAdr = request.queryString().get("mac")[0];

		// screen not registered
		if (screenRepository.getByMacAddress(macAdr) == null) {
			// TODO change index to send code for registering
			return ok(screen_code.render("1234"));
		}
		// screen registered
		else {
			// TODO check if screen is already logged in, if so redirects it directly to eventsource
			// else log it in first
			// TODO add mac addr to list of activated screens

			return ok(eventsource.render()).withCookies(
				Http.Cookie.builder("mac", macAdr)
				.withHttpOnly(false)
				.build());
		}
	}

	public Result register(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		if (screenRepository.getByMacAddress(boundForm.get("mac")) != null) {
			// screen is already known
			// TODO: return error (with error handling)
			return index();
		}
		else {
			Screen newScreen = new Screen(boundForm.get("mac"));
			String code = boundForm.get("code");

			// TODO check here code is correct

			screenRepository.add(newScreen);

			return redirect(routes.HomeController.index());
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
}
