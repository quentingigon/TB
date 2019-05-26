package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.DiffuserData;
import models.repositories.interfaces.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import views.html.diffuser.diffuser_activation;
import views.html.diffuser.diffuser_creation;
import views.html.diffuser.diffuser_page;
import views.html.diffuser.diffuser_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static services.BlockUtils.getBlockNumberOfTime;

public class DiffuserController extends Controller {

	private Form<DiffuserData> form;

	private final RunningScheduleThreadManager serviceManager;

	private final ServicePicker servicePicker;

	@Inject
	public DiffuserController(FormFactory formFactory,
							  RunningScheduleThreadManager serviceManager,
							  ServicePicker servicePicker) {
		this.servicePicker = servicePicker;
		this.form = formFactory.form(DiffuserData.class);
		this.serviceManager = serviceManager;
	}

	@With(UserAuthentificationAction.class)
	public Result index(Http.Request request) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(diffuser_page.render(servicePicker.getDiffuserService().getAllDiffusersOfTeam(teamId),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result indexWithErrorMessage(Http.Request request, String error) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(diffuser_page.render(servicePicker.getDiffuserService().getAllDiffusersOfTeam(teamId),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String name) {
		return ok(diffuser_update.render(form,
			new DiffuserData(name),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateViewWithErrorMessage(String name, String error) {
		return badRequest(diffuser_update.render(form,
			new DiffuserData(name),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result createView(Http.Request request) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(diffuser_creation.render(form,
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result createViewWithErrorMessage(Http.Request request, String error) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(diffuser_creation.render(form,
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result activateView(String name, Http.Request request) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(diffuser_activation.render(form,
			servicePicker.getScreenService().getAllScreensOfTeam(teamId),
			new DiffuserData(name),
			null,
			request));
	}

	@With(UserAuthentificationAction.class)
	public Result activateViewWithErrorMessage(String name, Http.Request request, String error) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(diffuser_activation.render(form,
			servicePicker.getScreenService().getAllScreensOfTeam(teamId),
			new DiffuserData(name),
			error,
			request));
	}

	@With(UserAuthentificationAction.class)
	public Result activate(Http.Request request) {

		final Form<DiffuserData> boundForm = form.bindFromRequest(request);
		DiffuserData data = boundForm.get();

		DiffuserService diffuserService = servicePicker.getDiffuserService();
		ScreenService screenService = servicePicker.getScreenService();
		ScheduleService scheduleService = servicePicker.getScheduleService();
		FluxService fluxService = servicePicker.getFluxService();

		Diffuser diffuser = diffuserService.getDiffuserByName(data.getName());

		// incorrect name
		if (diffuser == null) {
			return activateViewWithErrorMessage(data.getName(), request, "Diffuser name does not exists");
		}
		else {
			// get names of RunningSchedules concerned by the new Diffuser
			Set<Integer> scheduleIds = new HashSet<>();
			Set<Integer> runningScheduleIds = new HashSet<>();
			Set<Integer> screenIds = new HashSet<>();
			for (String mac: data.getScreens()) {

				Screen screen = screenService.getScreenByMacAddress(mac);
				if (screen == null) {
					return activateViewWithErrorMessage(data.getName(), request, "Screen MAC address does not exists");
				}
				screenIds.add(screen.getId());

				// get running schedule + all ids
				RunningSchedule rs = scheduleService.getRunningScheduleById(screen.getRunningScheduleId());
				runningScheduleIds.add(rs.getId());

				// get associated schedule
				scheduleIds.add(scheduleService.getScheduleById(rs.getScheduleId()).getId());
			}

			// no runningschedule are associated with the screens used by the diffuser
			if (runningScheduleIds.isEmpty()) {
				return activateViewWithErrorMessage(data.getName(), request, "None of the selected screens are active");
			}

			// Flux to add to schedules and services
			Flux diffusedFlux = fluxService.getFluxById(diffuser.getFlux());

			// update associated Schedule timetable
			for (Integer id: scheduleIds) {
				Schedule schedule = scheduleService.getScheduleById(id);
				// update schedule's timetable by adding a new entry for ScheduledFlux
				// and updating schedule
				ScheduledFlux sf = new ScheduledFlux();
				sf.setScheduleId(schedule.getId());
				sf.setStartBlock(diffuser.getStartBlock());
				sf.setFluxId(diffusedFlux.getId());

				sf = fluxService.createScheduled(sf);

				schedule.addToFluxes(sf.getId());

				scheduleService.update(schedule);
			}

			// update associated RunningScheduleThread
			for (Integer id: runningScheduleIds) {
				RunningScheduleThread rst = serviceManager.getServiceByScheduleId(id);
				if (rst != null) {
					// if diffuser has priority, it overwrites the timetable. Else it tries to schedule the flux at the given time
					// but does it not if not enough place
					if (diffuser.isOverwrite()) {
						rst.scheduleFluxFromDiffuser(diffusedFlux, diffuser.getStartBlock(), diffuser.getId());
					}
					else {
						rst.scheduleFluxIfPossibleFromDiffuser(diffusedFlux, diffuser.getStartBlock(), diffuser.getId());
					}
				}
			}

			// create new runningDiffuser
			RunningDiffuser rd = new RunningDiffuser(diffuser);
			rd.setDiffuserId(diffuser.getId());
			rd.setScreens(new ArrayList<>(screenIds));
			rd.setFluxId(diffusedFlux.getId());

			diffuserService.create(rd);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result deactivate(Http.Request request, String name) {
		DiffuserService diffuserService = servicePicker.getDiffuserService();
		ScreenService screenService = servicePicker.getScreenService();
		ScheduleService scheduleService = servicePicker.getScheduleService();
		FluxService fluxService = servicePicker.getFluxService();

		Diffuser diffuser = diffuserService.getDiffuserByName(name);

		// incorrect name
		if (diffuser == null) {
			return indexWithErrorMessage(request, "Diffuser name does not exist");
		}
		else {

			RunningDiffuser rd = diffuserService.getRunningDiffuserByDiffuserId(diffuser.getId());

			for (Integer id: diffuserService.getScreenIdsOfRunningDiffuserById(rd.getId())) {

				Screen screen = screenService.getScreenById(id);

				// if screen is active, update associated schedule
				if (screen.getRunningScheduleId() != null) {
					RunningSchedule rs = scheduleService.getRunningScheduleById(screen.getRunningScheduleId());

					Schedule schedule = scheduleService.getScheduleById(rs.getScheduleId());
					schedule.removeFromFluxes(diffuser.getFlux());

					scheduleService.update(schedule);
				}

				RunningScheduleThread rst = serviceManager.getServiceByScheduleId(id);
				if (rst != null) {
					rst.removeScheduledFluxFromDiffuser(
						fluxService.getFluxById(diffuser.getFlux()),
						diffuser.getId(),
						diffuser.getStartBlock()
					);
				}
			}
			diffuserService.delete(rd);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<DiffuserData> boundForm = form.bindFromRequest(request);
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());

		DiffuserService diffuserService = servicePicker.getDiffuserService();
		FluxService fluxService = servicePicker.getFluxService();
		TeamService teamService = servicePicker.getTeamService();

		DiffuserData data = boundForm.get();

		// diffuser already exists
		if (diffuserService.getDiffuserByName(data.getName()) != null) {
			return createViewWithErrorMessage(request, "Name is already taken");
		}
		else {
			Result error = checkDataIntegrity(data, "create", request);
			if (error != null) {
				return error;
			}
			Diffuser diffuser = new Diffuser(data.getName());
			diffuser.setFlux(fluxService.getFluxByName(data.getFluxName()).getId());
			diffuser.setValidity(Integer.valueOf(data.getValidity()));
			diffuser.setStartBlock(getBlockNumberOfTime(data.getStartTime()));
			diffuser.setOverwrite(data.isOverwrite());

			diffuser = diffuserService.create(diffuser);

			Team team = teamService.getTeamById(teamId);
			team.addToDiffusers(diffuser.getId());
			teamService.update(team);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<DiffuserData> boundForm = form.bindFromRequest(request);

		DiffuserService diffuserService = servicePicker.getDiffuserService();
		FluxService fluxService = servicePicker.getFluxService();

		DiffuserData data = boundForm.get();
		Diffuser diffuser = diffuserService.getDiffuserByName(data.getName());

		// name is incorrect
		if (diffuser == null) {
			return updateViewWithErrorMessage(data.getName(), "Diffuser name does not exists");
		}
		else {

			Result error = checkDataIntegrity(data, "update", request);
			if (error != null) {
				return error;
			}
			diffuser.setFlux(fluxService.getFluxByName(data.getFluxName()).getId());
			diffuser.setValidity(Integer.valueOf(data.getValidity()));
			diffuser.setName(data.getName());
			diffuser.setStartBlock(getBlockNumberOfTime(data.getStartTime()));

			diffuserService.update(diffuser);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(Http.Request request, String name) {

		DiffuserService diffuserService = servicePicker.getDiffuserService();

		Diffuser diffuser = diffuserService.getDiffuserByName(name);

		if (diffuser == null) {
			return indexWithErrorMessage(request, "Diffuser name does not exists");
		}
		else {
			diffuserService.delete(diffuser);

			return index(request);
		}
	}

	// TODO additional checks
	private Result checkDataIntegrity(DiffuserData data, String action, Http.Request request) {

		FluxService fluxService = servicePicker.getFluxService();
		ScreenService screenService = servicePicker.getScreenService();

		Result error = null;

		if (data.getValidity() == null) {
			if (action.equals("create"))
				error = createViewWithErrorMessage(request, "You must enter a validity");
			else if (action.equals("update"))
				error = updateViewWithErrorMessage(data.getName(), "You must enter a validity");
		}

		if (data.getFluxName() != null) {
			if (fluxService.getFluxByName(data.getFluxName()) ==  null) {
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
				if (screenService.getScreenByMacAddress(screenMAC) == null) {
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
