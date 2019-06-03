package controllers.actions;

import models.entities.UserData;
import models.repositories.interfaces.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import services.ServicePicker;
import views.html.user.user_login;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * This class represents an Action used to restrict access to Admin reserved resources
 */
public class AdminAuthentificationAction extends play.mvc.Action.Simple {


	private HttpExecutionContext executionContext;

	private final Form<UserData> form;
	private final ServicePicker servicePicker;

	@Inject
	public AdminAuthentificationAction(HttpExecutionContext executionContext,
									   FormFactory formFactory,
									   ServicePicker servicePicker) {
		this.servicePicker = servicePicker;
		this.executionContext = executionContext;
		this.form = formFactory.form(UserData.class);
	}

	public CompletionStage<Result> call(Http.Request req) {

		if (req.cookie("email") != null) {
			if (servicePicker.getUserService().getAdminByUserEmail(req.cookie("email").value()) != null) {
				return delegate.call(req);
			}
		}

		return CompletableFuture.supplyAsync(() -> "", executionContext.current())
			.thenApply(result -> badRequest(user_login.render(form,
				"You must be an admin to access this page")));
	}
}
