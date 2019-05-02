package controllers;

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
import services.RunningScheduleService;
import services.RunningScheduleServiceManager;
import views.html.diffuser_activation;
import views.html.diffuser_creation;
import views.html.diffuser_page;
import views.html.diffuser_update;

import javax.inject.Inject;
import javax.xml.ws.http.HTTPBinding;
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

	@Inject
	public DiffuserController(FormFactory formFactory) {
		this.form = formFactory.form(DiffuserData.class);
	}

	public Result index() {
		return ok(diffuser_page.render(getAllDiffusers(), null));
	}

	public Result updateView(String name) {
		return ok(diffuser_update.render(form, new DiffuserData(name), null));
	}

	public Result createView() {
		return ok(diffuser_creation.render(form, null));
	}

	public Result activateView(String name, Http.Request request) {
		return ok(diffuser_activation.render(form, getAllFluxes(), getAllScreens(), new DiffuserData(name), null, request));
	}

	// TODO activation page to select flux and screens
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
			Set<String> runningSchedulesNames = new HashSet<>();
			for (String mac: data.getScreens()) {
				runningSchedulesNames.add(screenRepository.getByMacAddress(mac).getRunningScheduleName());
			}

			// Flux to add to schedules and services
			Flux diffusedFlux = fluxRepository.getByName(data.getFluxName());

			// create new runningDiffuser
			RunningDiffuser rd = new RunningDiffuser(diffuser);
			rd.setFluxId(diffusedFlux.getId());
			rd.addToScreens(screenRepository.getByMacAddress("1234"));
			runningDiffuserRepository.add(rd);

			// update associated RunningSchedules
			for (String s: runningSchedulesNames) {
				RunningSchedule rs = runningScheduleRepository.getByName(s);
				rs.addToFluxes(diffusedFlux);
				runningScheduleRepository.update(rs);
			}

			// update associated RunningScheduleService
			// TODO maybe do this in addToFluxes method in RunningSchedule
			RunningScheduleServiceManager manager = RunningScheduleServiceManager.getInstance();
			for (String s: runningSchedulesNames) {
				RunningScheduleService rss = manager.getServiceByName(s);
				rss.addFluxToRunningSchedule(diffusedFlux);
			}


			return index();
		}
	}

	public Result deactivate(String name) {
		Diffuser diffuser = diffuserRepository.getByName(name);

		// incorrect name
		if (diffuser == null) {
			return badRequest(diffuser_page.render(getAllDiffusers(), "Diffuser name does not exist"));
		}
		else {

			runningDiffuserRepository.delete(runningDiffuserRepository.getByName(name));

			// modify associated RunningSchedule + modify associated service

			// runningScheduleRepository.getByName();

			RunningScheduleServiceManager manager = RunningScheduleServiceManager.getInstance();

			manager.removeRunningSchedule(name);


			// TODO remove from DB

			return index();
		}
	}

	public Result create(Http.Request request) {
		final Form<DiffuserData> boundForm = form.bindFromRequest(request);

		DiffuserData data = boundForm.get();

		// diffuser already exists
		if (diffuserRepository.getByName(data.getName()) != null) {
			return badRequest(diffuser_creation.render(form, "Name is already taken"));
		}
		else {
			Diffuser diffuser = new Diffuser(data.getName());

			diffuserRepository.add(diffuser);

			return index();
		}
	}

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
