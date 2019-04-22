package controllers;

import models.db.User;
import models.entities.UserData;
import models.repositories.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.login_test;
import views.html.user_register;

import javax.inject.Inject;

public class UserController extends Controller {

	@Inject
	UserRepository userRepository;

	private final Form<UserData> form;

	@Inject
	public UserController(FormFactory formFactory) {
		this.form = formFactory.form(UserData.class);
	}

	public Result registerView() {
		return ok(user_register.render(form));
	}

	public Result loginView() {
		return ok(login_test.render(form));
	}

	public Result register(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<UserData> boundForm = form.bindFromRequest(request);

		User newUser = new User(boundForm.get().getEmail(),
			boundForm.get().getPassword());

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
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<UserData> boundForm = form.bindFromRequest(request);

		UserData data = boundForm.get();

		User user = userRepository.get(data.getEmail(), data.getPassword());

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
