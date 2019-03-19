package controllers;

import models.Screen;
import models.repositories.ScreenRepository;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.eventsource;
import views.html.index;

import javax.inject.Inject;

public class ScreenController extends Controller {

	@Inject
	ScreenRepository screenRepository;

	public Result authentification(Http.Request request) {

		String macAdr = request.queryString().get("mac")[0];

		// TODO check if screen is already logged in

		Screen newScreen = new Screen(macAdr);

		if (screenRepository.getByMacAddress(macAdr) == null) {
			// screenRepository.add(newScreen);
			return ok(index.render("not in db"));
		}
		else {
			return ok(eventsource.render());
		}
	}
}
