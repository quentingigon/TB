package controllers;

import controllers.actions.UserAuthentificationAction;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.ServicePicker;
import views.html.index;

import javax.inject.Inject;

public class HomeController extends Controller {

    private final ServicePicker servicePicker;

    @Inject
    public HomeController(ServicePicker servicePicker) {
        this.servicePicker = servicePicker;
    }

    @With(UserAuthentificationAction.class)
    public Result index(Http.Request request) {
        Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());

        return ok(index.render(
            servicePicker.getScreenService().getAllActiveScreensOfTeam(teamId),
            servicePicker.getScheduleService().getAllActiveSchedulesOfTeam(teamId),
            servicePicker.getDiffuserService().getAllActiveDiffusersOfTeam(teamId),
            null));
    }
}
