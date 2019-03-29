package controllers;

import models.User;
import models.repositories.UserRepository;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.user_register;

import javax.inject.Inject;

public class UserController extends Controller {

	@Inject
	private FormFactory formFactory;

	@Inject
	UserRepository userRepository;

	public Result index() {
		return ok(user_register.render(formFactory.form(String.class)));
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
			return index();
		}
	}

	public Result login(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		User newUser = new User(boundForm.get("email"),
			boundForm.get("password"));

		return redirect(routes.HomeController.index());
	}

}
