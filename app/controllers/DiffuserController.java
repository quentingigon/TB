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
import java.util.ArrayList;
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

	@With(UserAuthentificationAction.class)
	public Result activate(Http.Request request) {

		final Form<DiffuserData> boundForm = form.bindFromRequest(request);

		DiffuserData data = boundForm.get();

		Diffuser diffuser = diffuserRepository.getByName(data.getName());

		// incorrect name
		if (diffuser == null) {
			return activateViewWithErrorMessage(data.getName(), request, "Diffuser name does not exists");
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

			// no runningschedule are associated with the screens used by the diffuser
			if (runningScheduleIds.isEmpty()) {
				return activateViewWithErrorMessage(data.getName(), request, "None of the selected screens are active");
			}

			// Flux to add to schedules and services
			Flux diffusedFlux = fluxRepository.getById(diffuser.getFlux());

			// update associated Schedule timetable
			for (Integer id: scheduleIds) {
				Schedule schedule = scheduleRepository.getById(id);
				// update schedule's timetable by adding a new entry for ScheduledFlux
				// and updating schedule
				ScheduledFlux sf = new ScheduledFlux();
				sf.setScheduleId(schedule.getId());
				sf.setStartBlock(diffuser.getStartBlock());
				sf.setFluxId(diffusedFlux.getId());
				sf = fluxRepository.addScheduledFlux(sf);
				schedule.addToFluxes(sf.getId());
				scheduleRepository.update(schedule);
			}

			// update associated RunningScheduleService
			for (Integer id: runningScheduleIds) {
				RunningScheduleService rss = serviceManager.getServiceByScheduleId(id);
				if (rss != null) {
					rss.scheduleFlux(diffusedFlux, diffuser.getStartBlock());
				}
			}

			// create new runningDiffuser
			RunningDiffuser rd = new RunningDiffuser(diffuser);
			rd.setDiffuserId(diffuser.getId());
			rd.addToScreens(screenRepository.getByMacAddress("1234").getId());
			rd.setFluxId(diffusedFlux.getId());
			runningDiffuserRepository.add(rd);

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

			RunningDiffuser rd = runningDiffuserRepository.getByDiffuserId(diffuser.getId());

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
			Result error = checkDataIntegrity(data, "create", request);
			if (error != null) {
				return error;
			}
			Diffuser diffuser = new Diffuser(data.getName());
			diffuser.setFlux(fluxRepository.getByName(data.getFluxName()).getId());
			diffuser.setValidity(Integer.valueOf(data.getValidity()));
			diffuser.setStartBlock(dataUtils.getBlockNumberOfTime(data.getStartTime()));

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

		DiffuserData data = boundForm.get();
		Diffuser diffuser = diffuserRepository.getByName(data.getName());

		// name is incorrect
		if (diffuser == null) {
			return updateViewWithErrorMessage(data.getName(), "Diffuser name does not exists");
		}
		else {

			Result error = checkDataIntegrity(data, "update", request);
			if (error != null) {
				return error;
			}
			diffuser.setFlux(fluxRepository.getByName(data.getFluxName()).getId());
			diffuser.setValidity(Integer.valueOf(data.getValidity()));
			diffuser.setName(data.getName());
			diffuser.setStartBlock(dataUtils.getBlockNumberOfTime(data.getStartTime()));

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

	// TODO additional checks
	private Result checkDataIntegrity(DiffuserData data, String action, Http.Request request) {

		Result error = null;

		if (data.getValidity() == null) {
			if (action.equals("create"))
				error = createViewWithErrorMessage(request, "You must enter a validity");
			else if (action.equals("update"))
				error = updateViewWithErrorMessage(data.getName(), "You must enter a validity");
		}

		if (data.getFluxName() != null) {
			if (fluxRepository.getByName(data.getFluxName()) ==  null) {
				if (action.equals("create"))
					error = createViewWithErrorMessage(request, "Flux does not exists");
				else if (action.equals("update"))
					error = updateViewWithErrorMessage(data.getName(), "Flux does not exists");

			}
		}
		else {
			data.setFluxName("");
		}

		if (data.getStartTime() == null) {
			if (action.equals("create"))
				error = createViewWithErrorMessage(request, "You must enter a start time");
			else if (action.equals("update"))
				error = updateViewWithErrorMessage(data.getName(), "You must enter a start time");
		}

		if (data.getScreens() != null) {
			for (String screenMAC: data.getScreens()) {
				if (screenRepository.getByMacAddress(screenMAC) == null) {
					if (action.equals("create"))
						error = createViewWithErrorMessage(request, "Screen MAC address does not exists");
					else if (action.equals("update"))
						error = updateViewWithErrorMessage(data.getName(), "Screen MAC address does not exists");
				}
			}
		}
		else {
			data.setScreens(new ArrayList<>());
		}

		return error;
	}
}
