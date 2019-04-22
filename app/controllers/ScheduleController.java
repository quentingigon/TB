package controllers;

import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Schedule;
import models.db.Screen;
import models.entities.ScheduleData;
import models.repositories.ScheduleRepository;
import models.repositories.ScreenRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.FluxManager;
import services.RunningScheduleService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ScheduleController extends Controller {

	@Inject
	ScheduleRepository scheduleRepository;

	@Inject
	ScreenRepository screenRepository;

	private Form<ScheduleData> form;

	@Inject
	public ScheduleController(FormFactory formFactory) {
		this.form = formFactory.form(ScheduleData.class);
	}

	// TODO maybe do it with entities (Data)
	public Result activate(String name) {

		Schedule scheduleToActivate = scheduleRepository.getByName(name);

		// incorrect name
		if (scheduleToActivate == null) {
			// TODO error + redirect
			return redirect(routes.HomeController.index());
		}
		else {

			RunningSchedule rs = new RunningSchedule(scheduleToActivate);

			Flux flux1 = new Flux("flux1", 10000, "https://heig-vd.ch/");
			Flux flux2 = new Flux("flux2", 10000, "https://hes-so.ch/");
			List<Flux> fluxes1 = new ArrayList<>();
			fluxes1.add(flux1);
			fluxes1.add(flux2);
			rs.setFluxes(fluxes1);

			Screen screen1 = new Screen("1234");
			Screen screen2 = new Screen("test");
			List<Screen> screens1 = new ArrayList<>();
			screens1.add(screen1);
			screens1.add(screen2);
			rs.setScreens(screens1);

			RunningScheduleService service = new RunningScheduleService(rs);
			service.addObserver(FluxManager.getInstance());

			Thread t = new Thread(service);
			t.start();

			// TODO fix -> need to make a correct subclass (weak entity) from Schedule
			scheduleRepository.add(rs);

			return redirect(routes.HomeController.index());
		}
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);

		// schedule already exists
		if (scheduleRepository.getByName(boundForm.get().getName()) != null) {
			// TODO error + redirect
			return redirect(routes.HomeController.index());
		}
		else {
			Schedule schedule = new Schedule(boundForm.get().getName());

			scheduleRepository.add(schedule);

			return redirect(routes.HomeController.index());
		}
	}

	public Result update(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);

		Schedule schedule = scheduleRepository.getByName(boundForm.get().getName());

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
