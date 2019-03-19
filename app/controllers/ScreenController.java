package controllers;

import models.Screen;
import models.repositories.ScreenRepository;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.eventsource;

import javax.inject.Inject;

public class ScreenController extends Controller {

	@Inject
	ScreenRepository screenRepository;

	public Result authentification(Http.Request request) {

		String macAdr = request.queryString().get("mac")[0];

		// TODO check if screen is already logged in

		Screen newScreen = new Screen(macAdr);

		// screenRepository.add(newScreen);

		String[] macAdresses = new String[] {macAdr, "test"};

		// set the concerned mac addresses in the cookie
		// Http.Cookie macCookie = Http.Cookie.builder("mac", String.join(",", macAdresses)).build();

		request.session().adding("mac", String.join(",", macAdresses));

		Result result = ok(eventsource.render());
		// result.session(request).adding("mac", String.join(",", macAdresses));
		return result;
	}
}
