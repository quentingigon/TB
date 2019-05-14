package controllers.actions;

import models.entities.UserData;
import models.repositories.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import views.html.user.user_login;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class TeamAdminAuthentificationAction extends play.mvc.Action.Simple {

	@Inject
	UserRepository userRepository;

	private HttpExecutionContext executionContext;

	private final Form<UserData> form;

	@Inject
	public TeamAdminAuthentificationAction(HttpExecutionContext executionContext, FormFactory formFactory) {
		this.executionContext = executionContext;
		this.form = formFactory.form(UserData.class);
	}

	public CompletionStage<Result> call(Http.Request req) {

		if (req.cookie("email") != null) {
			// TODO replace getAdmins by get TeamAdmin -> need triggers ?
			if (userRepository.getAdminByUserEmail(req.cookie("email").value()) != null) {
				return delegate.call(req);
			}
		}

		return CompletableFuture.supplyAsync(() -> "", executionContext.current())
			.thenApply(result -> badRequest(user_login.render(form, "You must be a team admin to access this page")));
	}
}
