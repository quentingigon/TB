public class AdminAuthentificationAction extends play.mvc.Action.Simple {
  private HttpExecutionContext executionContext;
  private final Form<UserData> form;

  @Inject
  public AdminAuthentificationAction(HttpExecutionContext executionContext,
                                     FormFactory formFactory) {
    this.executionContext = executionContext;
    this.form = formFactory.form(UserData.class);
  }

  public CompletionStage<Result> call(Http.Request req) {
    if (req.cookie("email") != null) {
      if (userService.getAdminByUserEmail(req.cookie("email").value()) != null) {
        return delegate.call(req);
      }
    }
    return CompletableFuture.supplyAsync(() -> "", executionContext.current())
      .thenApply(result -> badRequest(user_login.render(form,
        "You must be an admin to access this page")));
  }
}