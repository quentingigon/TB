package controllers;

import models.User;
import models.repositories.UserRepository;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.user_login;
import views.html.user_register;

import javax.inject.Inject;

public class UserController extends Controller {

	@Inject
	private FormFactory formFactory;

	@Inject
	UserRepository userRepository;

	public Result registerView() {
		return ok(user_register.render(formFactory.form(String.class)));
	}

	public Result loginView() {
		return ok(user_login.render(formFactory.form(String.class)));
	}

	public Result register(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		User newUser = new User(boundForm.get("email"),
			boundForm.get("password"));

		// email is unique
		if (userRepository.getByEmail(newUser.getEmail()) == null) {
			userRepository.create(newUser);

			return redirect(routes.HomeController.index());
		}
		else {
			// user must choose a new email
			return registerView();
		}
	}

	public Result login(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		User user = userRepository.get(boundForm.get("email"), boundForm.get("password"));

		if (user == null) {
			// TODO send error
			return loginView();
		}
		else {
			return redirect(routes.HomeController.index()).withHeader(
				"JWT", JwtUtils.getJWT());
		}
	}
}
