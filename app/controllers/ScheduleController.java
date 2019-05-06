package controllers;

import models.db.Flux;
import models.db.RunningSchedule;
import models.db.Schedule;
import models.db.Screen;
import models.entities.FluxData;
import models.entities.ScheduleData;
import models.entities.ScreenData;
import models.repositories.*;
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
	RunningScheduleRepository runningScheduleRepository;

	@Inject
	ScreenRepository screenRepository;

	@Inject
	FluxRepository fluxRepository;

	@Inject
	ScheduleFluxesRepository scheduleFluxesRepository;

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

		Schedule schedule = scheduleRepository.getByName(name);

		// incorrect name
		if (schedule == null) {
			// TODO error + redirect
			return badRequest(schedule_page.render(getAllSchedules(), "Schedule does not exist"));
		}
		else {

			RunningSchedule rs = new RunningSchedule(schedule);
			// TODO change this to use screens sent from frontend at activation
			for (Screen s : screenRepository.getAll()) {
				rs.addToScreens(s.getId());
			}
			// TODO errors
			if (runningScheduleRepository.getByScheduleId(schedule.getId()) == null) {
				runningScheduleRepository.add(rs);
			}

			List<Flux> fluxes = new ArrayList<>();

			for (Integer fluxid: scheduleFluxesRepository.getFluxesIdsByScheduleId(schedule.getId())) {
				fluxes.add(fluxRepository.getById(fluxid));
			}

			// add service as observer of FluxManager
			RunningScheduleService service1 = new RunningScheduleService(
				runningScheduleRepository.getByScheduleId(schedule.getId()),
				fluxes,
				// TODO change this to use screens sent from frontend at activation
				screenRepository.getAll());

			service1.addObserver(fluxManager);

			// the schedule is activated
			RunningScheduleServiceManager manager = RunningScheduleServiceManager.getInstance();
			manager.addRunningSchedule(schedule.getName(), service1);

			return index();
		}
	}

	public Result deactivate(String name) {
		Schedule schedule = scheduleRepository.getByName(name);

		// incorrect name
		if (schedule == null) {
			return badRequest(schedule_page.render(getAllSchedules(), "Schedule does not exist"));
		}
		else {
			RunningScheduleServiceManager manager = RunningScheduleServiceManager.getInstance();

			manager.removeRunningSchedule(name);

			// TODO delete
			// runningScheduleRepository.delete();

			return index();
		}
	}

	public Result create(Http.Request request) {
		// final DynamicForm boundForm = formFactory.form().bindFromRequest(request);
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);

		ScheduleData data = boundForm.get();

		// schedule already exists
		if (scheduleRepository.getByName(data.getName()) != null) {
			return badRequest(schedule_creation.render(form, getAllFluxes(), getAllFluxes(), "MAC address already exists", request));
		}
		else {
			Schedule schedule = new Schedule(data.getName());

			// TODO check for null
			for (String fluxName: data.getFluxes()) {
				schedule.addToFluxes(fluxRepository.getByName(fluxName).getId());
			}

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
