package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.error.maintenance;
import views.html.error.no_schedule;
import views.html.error.site_error;
import views.html.error.waiting_page;

public class ErrorPageController extends Controller {

	public Result maintenanceView() {
		return ok(maintenance.render());
	}

	public Result waitingView() {
		return ok(waiting_page.render());
	}

	public Result siteErrorView() {
		return ok(site_error.render());
	}

	public Result noScheduleView() {
		return ok(no_schedule.render());
	}
}
