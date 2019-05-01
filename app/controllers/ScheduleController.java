package controllers;

import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Schedule;
import models.db.Screen;
import models.entities.FluxData;
import models.entities.ScheduleData;
import models.entities.ScreenData;
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

	private final FluxManager fluxManager;

	private Form<ScheduleData> form;

	@Inject
	public ScheduleController(FormFactory formFactory, FluxManager fluxManager) {
		this.form = formFactory.form(ScheduleData.class);
		this.fluxManager = fluxManager;
		Thread t = new Thread(this.fluxManager);
		t.start();
	}

	public Result index() {
		return ok(schedule_page.render(getAllSchedules(), null));
	}

	public Result updateView(String name) {
		return ok(schedule_update.render(form, new ScheduleData(scheduleRepository.getByName(name)),
			getAllFluxes(), getAllFluxes(), null));
	}

	public Result createView(Http.Request request) {
		return ok(schedule_creation.render(form, getAllFluxes(), getAllFluxes(), null, request));
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

			RunningSchedule rs = new RunningSchedule(scheduleToActivate);

			List<Flux> fluxes = fluxRepository.getAll();
			rs.setFluxes(fluxes);

			List<Screen> screens = screenRepository.getAll();
			rs.setScreens(screens);

			RunningScheduleService service1 = new RunningScheduleService(rs);
			service1.addObserver(fluxManager);

			// the schedule is activated
			RunningScheduleServiceManager manager = RunningScheduleServiceManager.getInstance();
			manager.addRunningSchedule(service1);

			// TODO fix -> need to make a correct subclass (weak entity) from Schedule
			// scheduleRepository.add(rs);

			return index();
		}
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);

		ScheduleData data = boundForm.get();

		// schedule already exists
		if (scheduleRepository.getByName(data.getName()) != null) {
			return badRequest(schedule_creation.render(form, getAllFluxes(), getAllFluxes(), "MAC address does not exists", request));
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

	private List<ScreenData> getAllScreens() {
		List<ScreenData> data = new ArrayList<>();
		for (Screen s: screenRepository.getAll()) {
			data.add(new ScreenData(s));
		}
		return data;
	}
}
