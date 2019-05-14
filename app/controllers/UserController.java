package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.Admin;
import models.db.TeamMember;
import models.db.User;
import models.entities.DataUtils;
import models.entities.UserData;
import models.repositories.TeamRepository;
import models.repositories.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import views.html.user.user_login;
import views.html.user.user_page;
import views.html.user.user_register;
import views.html.user.user_update;

import javax.inject.Inject;

public class UserController extends Controller {

	@Inject
	UserRepository userRepository;

	@Inject
	TeamRepository teamRepository;

	private final Form<UserData> form;

	private DataUtils dataUtils;

	@Inject
	public UserController(FormFactory formFactory, DataUtils dataUtils) {
		this.dataUtils = dataUtils;
		this.form = formFactory.form(UserData.class);
	}

	public Result index() {
		return ok(user_page.render(dataUtils.getAllUsers(), null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String email) {
		return ok(user_update.render(form, new UserData(userRepository.getByEmail(email)), null));
	}

	public Result registerView() {
		return ok(user_register.render(form, dataUtils.getAllTeams(), null));
	}

	public Result loginView() {
		return ok(user_login.render(form, null));
	}

	public Result register(Http.Request request) {
		final Form<UserData> boundForm = form.bindFromRequest(request);

		UserData data = boundForm.get();

		User newUser = new User(data.getEmail(), data.getPassword());

		// email is not unique
		if (userRepository.getByEmail(newUser.getEmail()) != null) {
			return badRequest(user_register.render(form, dataUtils.getAllTeams(), "email is already used"));

		}
		else {

			newUser = userRepository.create(newUser);

			// user created is part of a team
			if (data.getTeam() != null) {
				TeamMember newMember = new TeamMember(newUser);

				// set team
				newMember.setTeamId(teamRepository.getByName(data.getTeam()).getId());

				userRepository.createMember(newMember);
			}
			// user created is of admin type
			else if (data.getAdmin() != null) {
				data.setAdmin(data.getAdmin().toLowerCase());
				if (data.getAdmin().equals("admin")) {
					userRepository.createAdmin(new Admin(newUser.getId()));
				}
			}

			return redirect(routes.HomeController.index()).withCookies(
				Http.Cookie.builder("email", newUser.getEmail())
					.withHttpOnly(false)
					.build());
		}
	}

	public Result login(Http.Request request) {
		final Form<UserData> boundForm = form.bindFromRequest(request);

		UserData data = boundForm.get();

		User user = userRepository.get(data.getEmail(), data.getPassword());

		if (user == null) {
			return badRequest(user_login.render(form, "Wrong user info"));
		}
		else {
			return redirect(routes.HomeController.index()).withCookies(
				Http.Cookie.builder("email", user.getEmail())
					.withHttpOnly(false)
					.build());
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<UserData> boundForm = form.bindFromRequest(request);

		UserData data = boundForm.get();
		User user = userRepository.getByEmail(data.getEmail());

		// email is incorrect
		if (user == null) {
			return badRequest(user_update.render(form, new UserData(data.getEmail()), "Wrong email address"));
		}
		else {
			// TODO maybe check if null
			// do changes to diffuser here
			user.setEmail(data.getEmail());
			user.setPassword(data.getPassword());

			userRepository.update(user);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String email) {

		User user = userRepository.getByEmail(email);

		// email is incorrect
		if (user == null) {
			return badRequest(user_page.render(dataUtils.getAllUsers(), "Wrong email address"));
		}
		else {
			userRepository.delete(user);

			return index();
		}
	}
}
