// TeamController.java
@With(AdminAuthentificationAction.class)
public Result index() {
  return ok(team_page.render(servicePicker.getTeamService().getAllTeams(),
    null));
}