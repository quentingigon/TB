package controllers;

import models.repositories.ScreenRepository;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.eventsource;
import views.html.screen_code;

import javax.inject.Inject;

public class ScreenController extends Controller {

	@Inject
	ScreenRepository screenRepository;

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
}
