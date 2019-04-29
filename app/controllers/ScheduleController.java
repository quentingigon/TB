package controllers;

import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Schedule;
import models.db.Screen;
import models.entities.FluxData;
import models.entities.ScheduleData;
import models.repositories.FluxRepository;
import models.repositories.ScheduleRepository;
import models.repositories.ScreenRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.FluxManager;
import services.RunningScheduleService;
import services.RunningScheduleServiceManager;
import views.html.schedule_creation;
import views.html.schedule_page;
import views.html.schedule_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class ScheduleController extends Controller {

	@Inject
	ScheduleRepository scheduleRepository;

	@Inject
	ScreenRepository screenRepository;

	@Inject
	FluxRepository fluxRepository;

	private Form<ScheduleData> form;

	@Inject
	public ScheduleController(FormFactory formFactory) {
		this.form = formFactory.form(ScheduleData.class);
	}

	public Result index() {
		return ok(schedule_page.render(getAllSchedules(), null));
	}

	public Result updateView(String name) {
		return ok(schedule_update.render(form, new ScheduleData(scheduleRepository.getByName(name)),
			getAllFluxes(), getAllFluxes(), null));
	}

	public Result createView() {
		return ok(schedule_creation.render(form, getAllFluxes(), getAllFluxes(), null));
	}

	// TODO maybe do it with entities (Data)
	public Result activate(String name) {

		Schedule scheduleToActivate = scheduleRepository.getByName(name);

		// incorrect name
		if (scheduleToActivate == null) {
			// TODO error + redirect
			return status(440, "Schedule does not exist");
		}
		else {

			RunningSchedule rs1 = new RunningSchedule(scheduleToActivate);
			RunningSchedule rs2 = new RunningSchedule(scheduleToActivate);

			Flux flux1 = new Flux("flux1", 20000L, "https://heig-vd.ch/");
			Flux flux2 = new Flux("flux2", 40000L, "https://hes-so.ch/");
			List<Flux> fluxes1 = new ArrayList<>();
			List<Flux> fluxes2 = new ArrayList<>();
			fluxes1.add(flux1);
			fluxes2.add(flux2);
			rs1.setFluxes(fluxes1);
			rs2.setFluxes(fluxes2);

			Screen screen1 = new Screen("1234");
			Screen screen2 = new Screen("test");
			List<Screen> screens1 = new ArrayList<>();
			List<Screen> screens2 = new ArrayList<>();
			screens1.add(screen1);
			screens2.add(screen2);
			rs1.setScreens(screens1);
			rs2.setScreens(screens2);

			RunningScheduleService service1 = new RunningScheduleService(rs1);
			RunningScheduleService service2 = new RunningScheduleService(rs2);
			service1.addObserver(FluxManager.getInstance());
			service2.addObserver(FluxManager.getInstance());

			// the schedule is activated
			RunningScheduleServiceManager manager = RunningScheduleServiceManager.getInstance();
			manager.addRunningSchedule(service1);
			manager.addRunningSchedule(service2);

			// TODO fix -> need to make a correct subclass (weak entity) from Schedule
			// scheduleRepository.add(rs);

			return redirect(routes.HomeController.index());
		}
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);

		ScheduleData data = boundForm.get();

		// schedule already exists
		if (scheduleRepository.getByName(data.getName()) != null) {
			return badRequest(schedule_creation.render(form, getAllFluxes(), getAllFluxes(), "MAC address does not exists"));
		}
		else {
			Schedule schedule = new Schedule(data.getName());

			scheduleRepository.add(schedule);

			return index();
		}
	}

	public Result update(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);

		Schedule schedule = scheduleRepository.getByName(boundForm.get().getName());

		// name is incorrect
		if (schedule == null) {
			// TODO error + correct redirect
			return badRequest(schedule_update.render(form, new ScheduleData(boundForm.get().getName()), getAllFluxes(), getAllFluxes(), "MAC address does not exists"));
		}
		else {
			// do changes to schedule here

			scheduleRepository.update(schedule);

			return index();
		}
	}

	public Result delete(String name) {

		Schedule schedule = scheduleRepository.getByName(name);

		// name is incorrect
		if (schedule == null) {
			// TODO error + correct redirect
			return badRequest(schedule_page.render(getAllSchedules(), "Name in incorrect"));
		}
		else {
			scheduleRepository.delete(schedule);

			return index();
		}
	}

	private List<FluxData> getAllFluxes() {
		List<FluxData> data = new ArrayList<>();
		for (Flux f: fluxRepository.getAll()) {
			data.add(new FluxData(f));
		}
		return data;
	}

	private List<ScheduleData> getAllSchedules() {
		List<ScheduleData> data = new ArrayList<>();
		for (Schedule s: scheduleRepository.getAll()) {
			data.add(new ScheduleData(s));
		}
		return data;
	}
}
