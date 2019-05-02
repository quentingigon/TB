package controllers;

import models.db.Diffuser;
import models.db.TeamMember;
import models.db.User;
import models.entities.DiffuserData;
import models.entities.UserData;
import models.repositories.TeamRepository;
import models.repositories.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.user_login;
import views.html.user_page;
import views.html.user_register;
import views.html.user_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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

	public Result index() {
		return ok(user_page.render(getAllUsers(), null));
	}
	public Result updateView(String email) {
		return ok(user_update.render(form, new UserData(userRepository.getByEmail(email)), null));
	}

	public Result registerView() {
		return ok(user_register.render(form, null));
	}

	public Result loginView() {
		return ok(user_login.render(form, null));
	}

	public Result register(Http.Request request) {
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

	public Result update(Http.Request request) {
		final Form<UserData> boundForm = form.bindFromRequest(request);

		User user = userRepository.getByEmail(boundForm.get().getEmail());

		// email is incorrect
		if (user == null) {
			// TODO error + correct redirect
			return badRequest();
		}
		else {
			// do changes to diffuser here
			userRepository.update(user);

			return index();
		}
	}

	public Result delete(String email) {

		User user = userRepository.getByEmail(email);

		// email is incorrect
		if (user == null) {
			// TODO error + correct redirect
			return badRequest();
		}
		else {
			userRepository.delete(user);

			return index();
		}
	}

	private List<UserData> getAllUsers() {
		List<UserData> data = new ArrayList<>();
		for (User u: userRepository.getAll()) {
			data.add(new UserData(u));
		}
		return data;
	}
}
