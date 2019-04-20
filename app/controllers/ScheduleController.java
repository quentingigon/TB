package controllers;

import models.db.Schedule;
import models.repositories.ScheduleRepository;
import play.data.DynamicForm;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;

public class ScheduleController extends Controller {

	@Inject
	private FormFactory formFactory;

	@Inject
	ScheduleRepository scheduleRepository;

	public Result create(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		// schedule already exists
		if (scheduleRepository.getByName(boundForm.get("name")) != null) {
			// TODO error + redirect
			return redirect(routes.HomeController.index());
		}
		else {
			Schedule schedule = new Schedule(boundForm.get("name"));

			scheduleRepository.add(schedule);

			return redirect(routes.HomeController.index());
		}
	}

	public Result update(Http.Request request) {
		final DynamicForm boundForm = formFactory.form().bindFromRequest(request);

		Schedule schedule = scheduleRepository.getByName(boundForm.get("name"));

		// name is incorrect
		if (schedule == null) {
			// TODO error + correct redirect
			return redirect(routes.HomeController.index());
		}
		else {
			// do changes to schedule here

			scheduleRepository.update(schedule);

			return redirect(routes.HomeController.index());
		}
	}

	public Result delete(String name) {

		Schedule schedule = scheduleRepository.getByName(name);

		// name is incorrect
		if (schedule == null) {
			// TODO error + correct redirect
			return redirect(routes.HomeController.index());
		}
		else {
			scheduleRepository.delete(schedule);

			return redirect(routes.HomeController.index());
		}
	}
}
