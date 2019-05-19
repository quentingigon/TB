package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.Admin;
import models.db.Team;
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

	public Result indexWithErrorMessage(String error) {
		return badRequest(user_page.render(dataUtils.getAllUsers(), error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String email) {
		return ok(user_update.render(form, new UserData(userRepository.getByEmail(email)), null));
	}

	public Result updateViewWithErrorMessage(String email, String error) {
		return badRequest(user_update.render(form, new UserData(email), error));
	}

	public Result registerView() {
		return ok(user_register.render(form, dataUtils.getAllTeams(), null));
	}

	public Result registerViewWithErrorMessage(String error) {
		return badRequest(user_register.render(form, dataUtils.getAllTeams(), error));
	}

	public Result loginView() {
		return ok(user_login.render(form, null));
	}

	public Result loginViewWithErrorMessage(String error) {
		return badRequest(user_login.render(form, error));
	}

	public Result register(Http.Request request) {
		final Form<UserData> boundForm = form.bindFromRequest(request);


		UserData data = boundForm.get();

		User newUser = new User(data.getEmail(), data.getPassword());

		// email is not unique
		if (userRepository.getByEmail(newUser.getEmail()) != null) {
			return registerViewWithErrorMessage("Email is already used");
		}
		else {

			newUser = userRepository.create(newUser);

			// TODO verify its correct
			// user created is part of a team
			if (data.getTeam() != null) {
				Team team = teamRepository.getByName(data.getTeam());
				TeamMember member = new TeamMember(newUser);
				member.setTeamId(teamRepository.getByName(team.getName()).getId());
				member = userRepository.createMember(member);

				if (data.getAdmin()) {
					team.addAdmin(member.getId());
					teamRepository.update(team);
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
			return loginViewWithErrorMessage("Wrong credentials");
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
			return updateViewWithErrorMessage(data.getEmail(), "Wrong email address");
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
			return indexWithErrorMessage("Wrong email address");
		}
		else {
			userRepository.delete(user);

			return index();
		}
	}
}
