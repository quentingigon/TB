package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.DiffuserData;
import models.entities.FluxData;
import models.entities.ScheduleData;
import models.entities.ScreenData;
import models.repositories.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.RunningScheduleService;
import services.RunningScheduleServiceManager;
import views.html.diffuser_activation;
import views.html.diffuser_creation;
import views.html.diffuser_page;
import views.html.diffuser_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiffuserController extends Controller {

	@Inject
	ScheduleRepository scheduleRepository;

	@Inject
	ScreenRepository screenRepository;

	@Inject
	FluxRepository fluxRepository;

	@Inject
	DiffuserRepository diffuserRepository;

	@Inject
	RunningScheduleRepository runningScheduleRepository;

	@Inject
	RunningDiffuserRepository runningDiffuserRepository;

	private Form<DiffuserData> form;

	private final RunningScheduleServiceManager serviceManager;

	@Inject
	public DiffuserController(FormFactory formFactory, RunningScheduleServiceManager serviceManager) {
		this.form = formFactory.form(DiffuserData.class);
		this.serviceManager = serviceManager;
	}

	@With(UserAuthentificationAction.class)
	public Result index() {
		return ok(diffuser_page.render(getAllDiffusers(), null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String name) {
		return ok(diffuser_update.render(form, new DiffuserData(name), null));
	}

	@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(diffuser_creation.render(form, getAllFluxes(), null));
	}

	@With(UserAuthentificationAction.class)
	public Result activateView(String name, Http.Request request) {
		return ok(diffuser_activation.render(form, getAllScreens(), new DiffuserData(name), null, request));
	}

	// TODO activation page to select flux and screens
	@With(UserAuthentificationAction.class)
	public Result activate(Http.Request request) {

		final Form<DiffuserData> boundForm = form.bindFromRequest(request);

		DiffuserData data = boundForm.get();

		Diffuser diffuser = diffuserRepository.getByName(data.getName());

		// incorrect name
		if (diffuser == null) {
			return badRequest(diffuser_page.render(getAllDiffusers(), "Diffuser name does not exist"));
		}
		else {

			// get names of RunningSchedules concerned by the new Diffuser
			Set<Integer> scheduleIds = new HashSet<>();
			Set<Integer> runningScheduleIds = new HashSet<>();
			for (String mac: data.getScreens()) {
				if (screenRepository.getByMacAddress(mac) == null) {
					return badRequest(diffuser_page.render(getAllDiffusers(), "Screen mac address does not exist"));
				}
				// get running schedule + all ids
				RunningSchedule rs = runningScheduleRepository.getById(screenRepository.getByMacAddress(mac).getRunningscheduleId());
				runningScheduleIds.add(rs.getId());

				// get associated schedule
				scheduleIds.add(scheduleRepository.getById(rs.getScheduleId()).getId());
			}

			// Flux to add to schedules and services
			Flux diffusedFlux = fluxRepository.getById(diffuser.getFlux());

			// create new runningDiffuser
			RunningDiffuser rd = new RunningDiffuser(diffuser);
			rd.setDiffuserId(diffuser.getId());
			rd.addToScreens(screenRepository.getByMacAddress("1234").getId());
			runningDiffuserRepository.add(rd);

			// update associated Schedule timetable
			for (Integer id: scheduleIds) {
				Schedule schedule = scheduleRepository.getById(id);
				// TODO update schedule's timetable
			}

			// update associated RunningScheduleService
			for (Integer id: runningScheduleIds) {
				RunningScheduleService rss = serviceManager.getServiceById(id);
				// TODO change 100 with correct value
				rss.scheduleFlux(diffusedFlux, 100);
			}


			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result deactivate(String name) {
		Diffuser diffuser = diffuserRepository.getByName(name);

		// incorrect name
		if (diffuser == null) {
			return badRequest(diffuser_page.render(getAllDiffusers(), "Diffuser name does not exist"));
		}
		else {

			RunningDiffuser rd = runningDiffuserRepository.getByName(name);

			Set<Integer> scheduleIds = new HashSet<>(rd.getScreens());

			runningDiffuserRepository.delete(rd);

			// TODO modify associated RunningSchedule

			for (Integer id: scheduleIds) {
				serviceManager.getServiceById(id).removeScheduledFlux(fluxRepository.getById(diffuser.getFlux()));
			}

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<DiffuserData> boundForm = form.bindFromRequest(request);

		DiffuserData data = boundForm.get();

		// diffuser already exists
		if (diffuserRepository.getByName(data.getName()) != null) {
			return badRequest(diffuser_creation.render(form, getAllFluxes(), "Name is already taken"));
		}
		else {
			Diffuser diffuser = new Diffuser(data.getName());
			diffuser.setFlux(fluxRepository.getByName(data.getFluxName()).getId());

			diffuserRepository.add(diffuser);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<DiffuserData> boundForm = form.bindFromRequest(request);

		Diffuser diffuser = diffuserRepository.getByName(boundForm.get().getName());

		// name is incorrect
		if (diffuser == null) {
			// TODO error + correct redirect
			return badRequest(diffuser_update.render(form, new DiffuserData(boundForm.get().getName()), "Diffuser name does not exists"));
		}
		else {
			// do changes to diffuser here
			diffuserRepository.update(diffuser);

			return index();
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String name) {

		Diffuser diffuser = diffuserRepository.getByName(name);

		// name is incorrect
		if (diffuser == null) {
			// TODO error + correct redirect
			return badRequest(diffuser_page.render(getAllDiffusers(), "Diffuser name does not exists"));
		}
		else {
			diffuserRepository.delete(diffuser);

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

	private List<DiffuserData> getAllDiffusers() {
		List<DiffuserData> data = new ArrayList<>();
		for (Diffuser d: diffuserRepository.getAll()) {
			data.add(new DiffuserData(d));
		}
		return data;
	}
}
