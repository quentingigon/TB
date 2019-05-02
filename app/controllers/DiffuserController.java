package controllers;

import models.db.Diffuser;
import models.db.Flux;
import models.db.Schedule;
import models.db.Screen;
import models.entities.DiffuserData;
import models.entities.FluxData;
import models.entities.ScheduleData;
import models.entities.ScreenData;
import models.repositories.DiffuserRepository;
import models.repositories.FluxRepository;
import models.repositories.ScheduleRepository;
import models.repositories.ScreenRepository;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.RunningScheduleService;
import services.RunningScheduleServiceManager;
import views.html.diffuser_creation;
import views.html.diffuser_page;
import views.html.diffuser_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public class DiffuserController extends Controller {

	@Inject
	ScheduleRepository scheduleRepository;

	@Inject
	ScreenRepository screenRepository;

	@Inject
	FluxRepository fluxRepository;

	@Inject
	DiffuserRepository diffuserRepository;

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

	public Result createView(Http.Request request) {
		return ok(diffuser_creation.render(form, null));
	}

	// TODO maybe do it with entities (Data)
	public Result activate(String name) {

		Diffuser diffuser = diffuserRepository.getByName(name);

		// incorrect name
		if (diffuser == null) {
			return badRequest();
		}
		else {

			// TODO create RunningDiffuser + modify associated RunningSchedule + modify associated service

			RunningScheduleServiceManager manager = RunningScheduleServiceManager.getInstance();

			RunningScheduleService rss = manager.getServiceByName(name);

			return index();
		}
	}

	public Result deactivate(String name) {
		Diffuser diffuser = diffuserRepository.getByName(name);

		// incorrect name
		if (diffuser == null) {
			return badRequest();
		}
		else {

			// TODO delete RunningDiffuser + modify associated RunningSchedule + modify associated service

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
			return badRequest();
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
			return badRequest();
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
			return badRequest();
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
