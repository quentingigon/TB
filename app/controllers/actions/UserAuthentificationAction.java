package controllers.actions;

import models.db.TeamMember;
import models.entities.UserData;
import models.repositories.interfaces.TeamRepository;
import models.repositories.interfaces.UserRepository;
import play.data.Form;
import play.data.FormFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Http;
import play.mvc.Result;
import services.ServicePicker;
import services.UserService;
import views.html.user.user_login;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class UserAuthentificationAction extends play.mvc.Action.Simple {

	private HttpExecutionContext executionContext;

	private final Form<UserData> form;

	private final ServicePicker servicePicker;

	@Inject
	public UserAuthentificationAction(HttpExecutionContext executionContext,
									  FormFactory formFactory,
									  ServicePicker servicePicker) {
		this.servicePicker = servicePicker;
		this.executionContext = executionContext;
		this.form = formFactory.form(UserData.class);
	}

	public CompletionStage<Result> call(Http.Request req) {

		if (req.cookie("email") != null) {
			UserService userService = servicePicker.getUserService();
			Integer teamId = userService.getTeamIdOfUserByEmail(req.cookie("email").value());
			TeamMember member = userService.getMemberByUserEmail(req.cookie("email").value());
			// if member of team or teamadmin or admin
			if ((member != null && servicePicker.getTeamService().getTeamById(teamId).getMembers().contains(member.getId())) ||
				userService.getAdminByUserEmail(req.cookie("email").value()) != null) {
				return delegate.call(req);
			}
		}

		return CompletableFuture.supplyAsync(() -> "", executionContext.current())
			.thenApply(result -> badRequest(user_login.render(form,
				"You must be logged in to access this page")));
	}
}
