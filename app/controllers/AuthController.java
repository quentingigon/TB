package controllers;

import models.User;
import models.repositories.UserRepository;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.auth;

import javax.inject.Inject;

public class AuthController extends Controller {

	@Inject
	private FormFactory formFactory;

	@Inject
	UserRepository userRepository;

	public Result index() {
		return ok(auth.render("test"));
	}

	public Result register() {
		User newUser = new User(formFactory.form().get("email"),
			formFactory.form().get("password"));

		if (userRepository.getByEmail(newUser.getEmail()) != null) {
			userRepository.add(newUser);

			return redirect(routes.HomeController.index());
		}
		else {
			return index();
		}
	}

}
