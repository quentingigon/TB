package controllers;

import models.entities.DataUtils;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import views.html.index;

import javax.inject.Inject;

public class HomeController extends Controller {

    private final DataUtils dataUtils;

    @Inject
    public HomeController(DataUtils dataUtils) {
        this.dataUtils = dataUtils;
    }

    public Result index(Http.Request request) {
        Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
        return ok(index.render(dataUtils.getAllActiveScreensOfTeam(teamId), null));
    }

}
