package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.*;
import models.repositories.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.RunningScheduleService;
import services.RunningScheduleServiceManager;
import views.html.diffuser.diffuser_activation;
import views.html.diffuser.diffuser_creation;
import views.html.diffuser.diffuser_page;
import views.html.diffuser.diffuser_update;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static services.BlockUtils.blockNumber;

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

	@Inject
	TeamRepository teamRepository;

	private Form<DiffuserData> form;

	private final RunningScheduleServiceManager serviceManager;

	private DataUtils dataUtils;

	@Inject
	public DiffuserController(FormFactory formFactory, RunningScheduleServiceManager serviceManager, DataUtils dataUtils) {
		this.dataUtils = dataUtils;
		this.form = formFactory.form(DiffuserData.class);
		this.serviceManager = serviceManager;
	}

	@With(UserAuthentificationAction.class)
	public Result index(Http.Request request) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(diffuser_page.render(dataUtils.getAllDiffusersOfTeam(teamId), null));
	}

	public Result indexWithErrorMessage(Http.Request request, String error) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(diffuser_page.render(dataUtils.getAllDiffusersOfTeam(teamId), error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String name) {
		return ok(diffuser_update.render(form, new DiffuserData(name), null));
	}

	public Result updateViewWithErrorMessage(String name, String error) {
		return badRequest(diffuser_update.render(form, new DiffuserData(name), error));
	}

	@With(UserAuthentificationAction.class)
	public Result createView(Http.Request request) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(diffuser_creation.render(form, dataUtils.getAllFluxesOfTeam(teamId), null));
	}

	public Result createViewWithErrorMessage(Http.Request request, String error) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(diffuser_creation.render(form, dataUtils.getAllFluxesOfTeam(teamId), error));
	}

	@With(UserAuthentificationAction.class)
	public Result activateView(String name, Http.Request request) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(diffuser_activation.render(form, dataUtils.getAllScreensOfTeam(teamId), new DiffuserData(name), null, request));
	}

	public Result activateViewWithErrorMessage(String name, Http.Request request, String error) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(diffuser_activation.render(form, dataUtils.getAllScreensOfTeam(teamId), new DiffuserData(name), error, request));
	}

	// TODO activation page to select flux and screens
	@With(UserAuthentificationAction.class)
	public Result activate(Http.Request request) {

		final Form<DiffuserData> boundForm = form.bindFromRequest(request);

		DiffuserData data = boundForm.get();

		Diffuser diffuser = diffuserRepository.getByName(data.getName());

		// incorrect name
		if (diffuser == null) {
			return activateViewWithErrorMessage(data.getName(), request, "Diffuser name does not exists");
		}
		else if (Integer.valueOf(data.getStartTime()) < 0 || Integer.valueOf(data.getStartTime()) > blockNumber) {
			return activateViewWithErrorMessage(data.getName(), request, "Start time must be between 0 and " + blockNumber);
		}
		else {

			// get names of RunningSchedules concerned by the new Diffuser
			Set<Integer> scheduleIds = new HashSet<>();
			Set<Integer> runningScheduleIds = new HashSet<>();
			for (String mac: data.getScreens()) {
				if (screenRepository.getByMacAddress(mac) == null) {
					return activateViewWithErrorMessage(data.getName(), request, "Screen MAC address does not exists");
				}
				// get running schedule + all ids
				RunningSchedule rs = runningScheduleRepository.getById(screenRepository.getByMacAddress(mac).getRunningScheduleId());
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
				// update schedule's timetable by adding a new entry for ScheduledFlux
				// and updating schedule
				ScheduledFlux sf = new ScheduledFlux();
				sf.setScheduleId(schedule.getId());
				sf.setStartBlock(Integer.valueOf(data.getStartTime()));
				sf.setFluxId(diffusedFlux.getId());
				sf = fluxRepository.addScheduledFlux(sf);
				schedule.addToFluxes(sf.getId());
				scheduleRepository.update(schedule);
			}

			// update associated RunningScheduleService
			for (Integer id: runningScheduleIds) {
				RunningScheduleService rss = serviceManager.getServiceByScheduleId(id);
				if (rss != null) {
					rss.scheduleFlux(diffusedFlux, Integer.valueOf(data.getStartTime()));
				}
			}

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result deactivate(Http.Request request, String name) {
		Diffuser diffuser = diffuserRepository.getByName(name);

		// incorrect name
		if (diffuser == null) {
			return indexWithErrorMessage(request, "Diffuser name does not exist");
		}
		else {

			RunningDiffuser rd = runningDiffuserRepository.getByName(name);

			Set<Integer> screenIds = new HashSet<>(rd.getScreens());
			for (Integer id: screenIds) {

				Screen screen = screenRepository.getById(id);

				if (screen.getRunningScheduleId() != null) {
					RunningSchedule rs = runningScheduleRepository.getById(screen.getRunningScheduleId());

					Schedule schedule = scheduleRepository.getById(rs.getScheduleId());
					schedule.removeFromFluxes(diffuser.getFlux());
					scheduleRepository.update(schedule);

				}
				serviceManager.getServiceByScheduleId(id).removeScheduledFlux(fluxRepository.getById(diffuser.getFlux()));
			}

			runningDiffuserRepository.delete(rd);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<DiffuserData> boundForm = form.bindFromRequest(request);
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());

		DiffuserData data = boundForm.get();

		// diffuser already exists
		if (diffuserRepository.getByName(data.getName()) != null) {
			return createViewWithErrorMessage(request, "Name is already taken");
		}
		else {
			Diffuser diffuser = new Diffuser(data.getName());
			diffuser.setFlux(fluxRepository.getByName(data.getFluxName()).getId());
			diffuser.setValidity(Integer.valueOf(data.getValidity()));

			diffuser = diffuserRepository.add(diffuser);

			Team team = teamRepository.getById(teamId);
			team.addToDiffusers(diffuser.getId());
			teamRepository.update(team);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<DiffuserData> boundForm = form.bindFromRequest(request);

		Diffuser diffuser = diffuserRepository.getByName(boundForm.get().getName());

		// name is incorrect
		if (diffuser == null) {
			return updateViewWithErrorMessage(boundForm.get().getName(), "Diffuser name does not exists");
		}
		else {


			// do changes to diffuser here
			diffuserRepository.update(diffuser);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(Http.Request request, String name) {

		Diffuser diffuser = diffuserRepository.getByName(name);

		if (diffuser == null) {
			return indexWithErrorMessage(request, "Diffuser name does not exists");
		}
		else {
			diffuserRepository.delete(diffuser);

			return index(request);
		}
	}
}
