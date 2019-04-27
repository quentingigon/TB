package controllers;

import models.db.TeamMember;
import models.db.User;
import models.entities.UserData;
import models.repositories.TeamRepository;
import models.repositories.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.user_login;
import views.html.user_register;

import javax.inject.Inject;

public class UserController extends Controller {

	@Inject
	UserRepository userRepository;

	@Inject
	TeamRepository teamRepository;

	private final Form<UserData> form;

	@Inject
	public UserController(FormFactory formFactory) {
		this.form = formFactory.form(UserData.class);
	}

	public Result registerView() {
		return ok(user_register.render(form, null));
	}

	public Result loginView() {
		return ok(user_login.render(form, null));
	}

	public Result register(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<UserData> boundForm = form.bindFromRequest(request);

		User newUser = new User(boundForm.get().getEmail(),
			boundForm.get().getPassword());

		// email is unique
		if (userRepository.getByEmail(newUser.getEmail()) == null) {

			// user created is part of a team
			if (boundForm.get().getTeam() != null) {
				TeamMember newMember = new TeamMember(newUser);

				// set team
				newMember.setTeam(teamRepository.getByName(boundForm.get().getTeam()));

//				userRepository.createMember(newMember);
			}
			else {
				userRepository.create(newUser);
			}

			return redirect(routes.HomeController.index()).withCookies(
				Http.Cookie.builder("logged", "true")
					.withHttpOnly(false)
					.build());
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
			return redirect(routes.HomeController.index()).withCookies(
				Http.Cookie.builder("logged", "true")
					.withHttpOnly(false)
					.build());
		}
	}
}
