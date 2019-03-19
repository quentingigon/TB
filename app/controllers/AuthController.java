package controllers;

import models.User;
import models.repositories.UserRepository;
import play.data.DynamicForm;
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

	public Result register(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		User newUser = new User(boundForm.get("email"),
			boundForm.get("password"));

		userRepository.create(newUser);

		return redirect(routes.HomeController.index());
		//return userRepository.add(newUser)
		//	.thenApplyAsync(u -> redirect(routes.HomeController.index()));
	}

}
