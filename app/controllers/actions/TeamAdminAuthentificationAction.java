package controllers.actions;

import models.db.TeamMember;
import models.entities.DataUtils;
import models.entities.UserData;
import models.repositories.interfaces.TeamRepository;
import models.repositories.interfaces.UserRepository;
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

	@Inject
	TeamRepository teamRepository;

	private HttpExecutionContext executionContext;

	private final Form<UserData> form;

	private final DataUtils dataUtils;

	@Inject
	public TeamAdminAuthentificationAction(HttpExecutionContext executionContext,
										   FormFactory formFactory,
										   DataUtils dataUtils) {
		this.dataUtils = dataUtils;
		this.executionContext = executionContext;
		this.form = formFactory.form(UserData.class);
	}

	public CompletionStage<Result> call(Http.Request req) {

		if (req.cookie("email") != null) {
			Integer teamId = dataUtils.getTeamIdOfUserByEmail(req.cookie("email").value());
			TeamMember member = userRepository.getMemberByUserEmail(req.cookie("email").value());
			// if we are admin or teamadmin
			if ((member != null && teamRepository.getById(teamId).getAdmins().contains(member.getId())) &&
				userRepository.getAdminByUserEmail(req.cookie("email").value()) != null) {
				return delegate.call(req);
			}
		}

		return CompletableFuture.supplyAsync(() -> "", executionContext.current())
			.thenApply(result -> badRequest(user_login.render(form, "You must be a team admin to access this page")));
	}
}
