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
import views.html.schedule_activation;
import views.html.schedule_creation;
import views.html.schedule_page;
import views.html.schedule_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static services.BlockUtils.blockNumber;

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

	private final RunningScheduleServiceManager serviceManager;

	private Form<ScheduleData> form;

	@Inject
	public ScheduleController(FormFactory formFactory, FluxManager fluxManager, RunningScheduleServiceManager serviceManager) {
		this.form = formFactory.form(ScheduleData.class);
		this.fluxManager = fluxManager;
		this.serviceManager = serviceManager;
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

	public Result activateView(String name, Http.Request request) {
		return ok(schedule_activation.render(form, getAllScreens(), new ScheduleData(name), null, request));
	}

	// TODO implement on frontend a way to choose an hour for a flux and then use table scheduled_flux to persist the info
	public Result activate(Http.Request request) {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);

		ScheduleData data = boundForm.get();

		Schedule schedule = scheduleRepository.getByName(data.getName());

		// incorrect name
		if (schedule == null) {
			return badRequest(schedule_page.render(getAllSchedules(), "Schedule does not exist"));
		}
		else {

			RunningSchedule rs = new RunningSchedule(schedule);
			if (runningScheduleRepository.getByScheduleId(schedule.getId()) != null) {
				return badRequest(schedule_page.render(getAllSchedules(), "This schedule is already activated"));
			}
			rs = runningScheduleRepository.add(rs);

			List<Screen> screens = new ArrayList<>();
			for (String screenMac : data.getScreens()) {
				Screen screen = screenRepository.getByMacAddress(screenMac);
				if (screen == null) {
					return badRequest(schedule_page.render(getAllSchedules(), "screen mac address does not exist : " + screenMac));
				}
				rs.addToScreens(screenRepository.getByMacAddress(screenMac).getId());
				screen.setRunningscheduleId(rs.getId());

				screens.add(screen);
				screenRepository.update(screen);
				runningScheduleRepository.update(rs);
			}

			// add service as observer of FluxManager
			RunningScheduleService service2 = new RunningScheduleService(
				runningScheduleRepository.getByScheduleId(schedule.getId()),
				screens,
				fluxRepository.getAll(), // fallbackfluxes TODO
				getTimeTable(schedule));

			service2.addObserver(fluxManager);

			// the schedule is activated
			serviceManager.addRunningSchedule(schedule.getId(), service2);

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
			RunningSchedule rs = runningScheduleRepository.getByScheduleId(schedule.getId());

			// remove RunningSchedule reference from all concerned screens
			for (Screen s : screenRepository.getAllByRunningScheduleId(rs.getId())) {
				s.setRunningscheduleId(null);
				screenRepository.update(s);
			}

			runningScheduleRepository.delete(rs);

			serviceManager.removeRunningSchedule(schedule.getId());

			return index();
		}
	}

	public Result create(Http.Request request) {
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
				if (fluxRepository.getByName(fluxName) == null) {
					return badRequest(schedule_creation.render(form, getAllFluxes(), getAllFluxes(), "Flux name does not exists", request));
				}
				schedule.addToFluxes(fluxRepository.getByName(fluxName).getId());

			}

			scheduleRepository.add(schedule);

			return index();
		}
	}

	public Result update(Http.Request request) {
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

	// TODO integrate with schedule etc
	private HashMap<Integer, Flux> getTimeTable(Schedule schedule) {

		HashMap<Integer, Flux> timetable = new HashMap<>();
		for (int i = 0; i < blockNumber; i++) {
			if (i == 1)
				timetable.put(i, fluxRepository.getById(1));
			else
				timetable.put(i, null);
		}
		return timetable;
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
