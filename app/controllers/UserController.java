package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.Team;
import models.db.TeamMember;
import models.db.User;
import models.entities.UserData;
import models.repositories.interfaces.TeamRepository;
import models.repositories.interfaces.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.ServicePicker;
import services.TeamService;
import services.UserService;
import views.html.user.user_login;
import views.html.user.user_page;
import views.html.user.user_register;
import views.html.user.user_update;

import javax.inject.Inject;

public class UserController extends Controller {

	private final Form<UserData> form;

	private final ServicePicker servicePicker;

	@Inject
	public UserController(FormFactory formFactory, ServicePicker servicePicker) {
		this.servicePicker = servicePicker;
		this.form = formFactory.form(UserData.class);
	}

	public Result index() {
		return ok(user_page.render(servicePicker.getUserService().getAllUsers(),
			null));
	}

	public Result indexWithErrorMessage(String error) {
		return badRequest(user_page.render(servicePicker.getUserService().getAllUsers(),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String email) {
		return ok(user_update.render(form,
			new UserData(servicePicker.getUserService().getUserByEmail(email)),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateViewWithErrorMessage(String email, String error) {
		return badRequest(user_update.render(form,
			new UserData(email),
			error));
	}

	public Result registerView() {
		return ok(user_register.render(form,
			servicePicker.getTeamService().getAllTeams(),
			null));
	}

	public Result registerViewWithErrorMessage(String error) {
		return badRequest(user_register.render(form,
			servicePicker.getTeamService().getAllTeams(),
			error));
	}

	public Result loginView() {
		return ok(user_login.render(form,
			null));
	}

	public Result loginViewWithErrorMessage(String error) {
		return badRequest(user_login.render(form,
			error));
	}

	public Result register(Http.Request request) {
		final Form<UserData> boundForm = form.bindFromRequest(request);
		UserData data = boundForm.get();

		User newUser = new User(data.getEmail(), data.getPassword());

		UserService userService = servicePicker.getUserService();
		TeamService teamService = servicePicker.getTeamService();

		// email is not unique
		if (userService.getUserByEmail(newUser.getEmail()) != null) {
			return registerViewWithErrorMessage("Email is already used");
		}
		else {

			newUser = userService.createUser(newUser);

			// TODO verify its correct
			// user created is part of a team
			if (data.getTeam() != null) {
				Team team = teamService.getTeamByName(data.getTeam());
				TeamMember member = new TeamMember(newUser);
				member.setTeamId(teamService.getTeamByName(team.getName()).getId());
				member = userService.createTeamMember(member);

				if (data.getAdmin()) {
					team.addAdmin(member.getId());
					teamService.update(team);
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

		UserService userService = servicePicker.getUserService();

		User user = userService.getUserByEmailAndPassword(data.getEmail(), data.getPassword());

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
		UserService userService = servicePicker.getUserService();
		User user = userService.getUserByEmail(data.getEmail());

		// email is incorrect
		if (user == null) {
			return updateViewWithErrorMessage(data.getEmail(), "Wrong email address");
		}
		else {
			// TODO maybe check if null
			// do changes to diffuser here
			user.setEmail(data.getEmail());
			user.setPassword(data.getPassword());

			userService.updateUser(user);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String email) {

		UserService userService = servicePicker.getUserService();
		User user = userService.getUserByEmail(email);

		// email is incorrect
		if (user == null) {
			return indexWithErrorMessage("Wrong email address");
		}
		else {
			userService.deleteUser(user);

			return index();
		}
	}
}
