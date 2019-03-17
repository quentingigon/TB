package controllers;

import models.User;
import models.repositories.UserRepository;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

public class AuthController extends Controller {

	@Inject
	private MessagesApi messagesApi;

	@Inject
	private FormFactory formFactory;

	@Inject
	UserRepository userRepository;

	public Result index(Http.Request request) {
		return ok(views.html.register.render(formFactory.form(User.class), request, messagesApi.preferred(request)));
	}

	public Result register() {
		User newUser = new User(formFactory.form().get("email"),
			formFactory.form().get("password"));

		if (userRepository.getByEmail(newUser.getEmail()) != null) {
			userRepository.add(newUser);

			return redirect(routes.HomeController.index());
		}
		else {
			return redirect(routes.AuthController.index());
		}
	}

}
