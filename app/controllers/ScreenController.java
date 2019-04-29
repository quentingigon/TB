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

	public Result index() {

		return (ok(screen_page.render(getAllScreens(), null)));
	}

	public Result registerView() {
		return ok(screen_register.render(form, null));
	}

	public Result updateView(String mac) {
		return ok(screen_update.render(form, new ScreenData(screenRepository.getByMacAddress(mac)), null));
	}

	public Result authentification(Http.Request request) {

		String macAdr = request.queryString().get("mac")[0];

		Screen screen = screenRepository.getByMacAddress(macAdr);

		// screen not registered
		if (screen == null) {
			String code = screenRegisterCodeGenerator();

			waitingScreenRepository.add(new WaitingScreen(macAdr, code));

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

	public Result register(Http.Request request) {
		//final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<ScreenData> boundForm = form.bindFromRequest(request);

		String macAdr = boundForm.get().getMac();

		// screen is already known
		if (screenRepository.getByMacAddress(macAdr) != null) {
			return badRequest(screen_register.render(form, "Screen already exists, by MAC"));
		}
		else {
			Screen newScreen = new Screen(macAdr);
			String code = boundForm.get().getCode();

			// if code is correct
			if (waitingScreenRepository.getByMac(macAdr).getCode().equals(code)) {

				newScreen.setSiteId(siteRepository.getByName(boundForm.get().getSite()).getId());

				screenRepository.add(newScreen);

				return index();
			}
			else {
				// wrong code
				return registerView();
			}
		}
	}

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
			screen.setName(data.getName());
			screen.setMacAddress(data.getMac());
			screen.setResolution(data.getResolution());
			screen.setSiteId(siteRepository.getByName(data.getSite()).getId());

			screenRepository.update(screen);
			return index();
		}
	}

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
