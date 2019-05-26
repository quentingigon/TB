package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.DataUtils;
import models.entities.ScheduleData;
import models.repositories.interfaces.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import views.html.schedule.schedule_activation;
import views.html.schedule.schedule_creation;
import views.html.schedule.schedule_page;
import views.html.schedule.schedule_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static services.BlockUtils.*;

public class ScheduleController extends Controller {

	@Inject
	FluxRepository fluxRepository;

	private final FluxManager fluxManager;
	private final RunningScheduleThreadManager serviceManager;
	private final FluxChecker fluxChecker;

	private Form<ScheduleData> form;

	private final ServicePicker servicePicker;
	private final DataUtils dataUtils;

	@Inject
	public ScheduleController(FormFactory formFactory,
							  FluxManager fluxManager,
							  RunningScheduleThreadManager serviceManager,
							  ServicePicker servicePicker,
							  DataUtils dataUtils,
							  FluxChecker fluxChecker) {
		this.fluxChecker = fluxChecker;
		this.dataUtils = dataUtils;
		this.form = formFactory.form(ScheduleData.class);
		this.fluxManager = fluxManager;
		this.serviceManager = serviceManager;
		this.servicePicker = servicePicker;
		Thread t = new Thread(this.fluxManager);
		t.start();
	}

	@With(UserAuthentificationAction.class)
	public Result index(Http.Request request) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_page.render(servicePicker.getScheduleService().getAllSchedulesOfTeam(teamId),
			null));
	}

	@With(UserAuthentificationAction.class)
	private Result indexWithErrorMessage(Http.Request request, String error) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(schedule_page.render(servicePicker.getScheduleService().getAllSchedulesOfTeam(teamId),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(Http.Request request, String name) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_update.render(form,
			new ScheduleData(servicePicker.getScheduleService().getScheduleByName(name)),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllLocatedFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllGeneralFluxesOfTeam(teamId),
			null));
	}

	@With(UserAuthentificationAction.class)
	private Result updateViewWithErrorMessage(Http.Request request, String name, String error) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_update.render(form,
			new ScheduleData(servicePicker.getScheduleService().getScheduleByName(name)),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllLocatedFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllGeneralFluxesOfTeam(teamId),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result createView(Http.Request request) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_creation.render(form,
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			null,
			request));
	}

	@With(UserAuthentificationAction.class)
	private Result createViewWithErrorMessage(Http.Request request, String error) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_creation.render(form,
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			error,
			request));
	}

	@With(UserAuthentificationAction.class)
	public Result activateView(String name, Http.Request request) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_activation.render(form,
			servicePicker.getScreenService().getAllScreensOfTeam(teamId),
			new ScheduleData(name),
			null,
			request));
	}

	@With(UserAuthentificationAction.class)
	public Result activate(Http.Request request) {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);
		ScheduleService scheduleService = servicePicker.getScheduleService();
		ScreenService screenService = servicePicker.getScreenService();
		ScheduleData data = boundForm.get();

		Schedule schedule = scheduleService.getScheduleByName(data.getName());

		// incorrect name
		if (schedule == null) {
			return indexWithErrorMessage(request, "Schedule does not exist");
		}
		else {

			// create runningSchedule
			RunningSchedule rs = new RunningSchedule(schedule);
			if (scheduleService.getRunningScheduleByScheduleId(schedule.getId()) != null) {
				return indexWithErrorMessage(request, "This schedule is already activated");
			}
			rs = scheduleService.create(rs);

			List<Screen> screens = new ArrayList<>();
			for (String screenMac : data.getScreens()) {
				Screen screen = screenService.getScreenByMacAddress(screenMac);
				if (screen == null) {
					return indexWithErrorMessage(request, "screen mac address does not exist : " + screenMac);
				}
				rs.addToScreens(screen.getId());
				screen.setRunningscheduleId(rs.getId());
				screen.setActive(true);

				screens.add(screen);

				screenService.update(screen);
			}
			scheduleService.update(rs);

			// add service as observer of FluxManager
			RunningScheduleThread service2 = new RunningScheduleThread(
				rs,
				screens,
				new ArrayList<>(schedule.getFallbacks()),
				dataUtils.getTimeTable(schedule),
				fluxRepository,
				fluxChecker);

			service2.addObserver(fluxManager);

			// the schedule is activated
			serviceManager.addRunningSchedule(schedule.getId(), service2);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result deactivate(String name, Http.Request request) {
		ScheduleService scheduleService = servicePicker.getScheduleService();
		Schedule schedule = scheduleService.getScheduleByName(name);

		// incorrect name
		if (schedule == null) {
			return indexWithErrorMessage(request, "Schedule does not exists");
		}
		else {
			RunningSchedule rs = scheduleService.getRunningScheduleByScheduleId(schedule.getId());

			if (rs == null) {
				return indexWithErrorMessage(request, "Schedule is not activated");
			}

			scheduleService.delete(rs);

			serviceManager.removeRunningSchedule(schedule.getId());

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		ScheduleService scheduleService = servicePicker.getScheduleService();
		TeamService teamService = servicePicker.getTeamService();

		ScheduleData data = boundForm.get();

		if (data.getName().equals("")) {
			return createViewWithErrorMessage(request, "You must enter a name for the new schedule");
		}
		// schedule already exists
		else if (scheduleService.getScheduleByName(data.getName()) != null) {
			return createViewWithErrorMessage(request, "Schedule name already exists");
		}
		else {
			Schedule schedule = new Schedule(data.getName());

			Result error = checkFluxesIntegrity(data, request);
			if (error != null) {
				return error;
			}

			schedule.setFallbacks(new HashSet<>(getFallbackFluxIds(data)));
			schedule = scheduleService.create(schedule);

			for (ScheduledFlux sf: getScheduledFluxesFromData(data)) {
				sf.setScheduleId(schedule.getId());
				sf = servicePicker.getFluxService().createScheduled(sf);
				schedule.addToFluxes(sf.getId());
			}
			scheduleService.update(schedule);

			// add new schedule to current user's team
			Team team = teamService.getTeamById(teamId);
			team.addToSchedules(schedule.getId());
			teamService.update(team);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);

		ScheduleService scheduleService = servicePicker.getScheduleService();

		ScheduleData data = boundForm.get();
		Schedule schedule = scheduleService.getScheduleByName(data.getName());


		// name is incorrect
		if (schedule == null) {
			return updateViewWithErrorMessage(request, data.getName(), "Schedule name does not exists");
		}
		// schedule is activated
		else if (scheduleService.getRunningScheduleByScheduleId(schedule.getId()) != null) {
			return updateViewWithErrorMessage(request, data.getName(), "You can not update a schedule that is running");
		}
		else {

			Result error = checkFluxesIntegrity(data, request);
			if (error != null) {
				return error;
			}
			for (Integer fluxId: getFallbackFluxIds(data)) {
				schedule.addToFallbacks(fluxId);
			}

			// TODO Warning: there are no checks to avoid bad timetable init -> overlapping fluxes, etc
			for (ScheduledFlux sf: getScheduledFluxesFromData(data)) {
				sf.setScheduleId(schedule.getId());
				sf = servicePicker.getFluxService().createScheduled(sf);
				schedule.addToFluxes(sf.getId());
			}
			scheduleService.update(schedule);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String name, Http.Request request) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		ScheduleService scheduleService = servicePicker.getScheduleService();

		Schedule schedule = scheduleService.getScheduleByName(name);

		// name is incorrect
		if (schedule == null) {
			return badRequest(schedule_page.render(servicePicker.getScheduleService().getAllSchedulesOfTeam(teamId), "Name in incorrect"));
		}
		else {
			scheduleService.delete(schedule);

			return index(request);
		}
	}

	private Result checkFluxesIntegrity(ScheduleData data, Http.Request request) {
		Result error = null;
		for (String fluxData: data.getFluxes()) {

			String[] fluxDatas = fluxData.split("#");
			String fluxName = fluxDatas[0];

			if (fluxName == null || servicePicker.getFluxService().getFluxByName(fluxName) == null) {
				error = createViewWithErrorMessage(request, "Flux name does not exists");
			}

			// we must create a ScheduledFlux for this entry
			if (fluxDatas.length == 2 && !fluxDatas[1].equals("")) {
				String fluxTime = fluxDatas[1];
				int fluxHour = Integer.parseInt(fluxTime.split((":"))[0]);

				if (fluxHour < beginningHour || fluxHour > endHour) {
					error = createViewWithErrorMessage(request,
						"Time for flux: " + fluxName + " is not within bounds: " + beginningHour + " -> " + endHour);
				}
			}
		}
		return error;
	}

	private List<ScheduledFlux> getScheduledFluxesFromData(ScheduleData data) {
		List<ScheduledFlux> scheduledFluxes = new ArrayList<>();

		for (String fluxData: data.getFluxes()) {

			String[] fluxDatas = fluxData.split("#");
			String fluxName = fluxDatas[0];

			// we must create a ScheduledFlux for this entry
			if (fluxDatas.length == 2 && !fluxDatas[1].equals("")) {
				String fluxTime = fluxDatas[1];

				ScheduledFlux sf = new ScheduledFlux();
				sf.setFluxId(servicePicker.getFluxService().getFluxByName(fluxName).getId());
				sf.setStartBlock(getBlockNumberOfTime(fluxTime));
				scheduledFluxes.add(sf);
			}
		}
		return scheduledFluxes;
	}

	private List<Integer> getFallbackFluxIds(ScheduleData data) {

		List<Integer> fallbacksIds = new ArrayList<>();

		for (String fluxData : data.getFluxes()) {
			String[] fluxDatas = fluxData.split("#");
			String fluxName = fluxDatas[0];

			// we must add a un-scheduled flux -> fallback flux
			if (fluxDatas.length != 2) {
				fallbacksIds.add(servicePicker.getFluxService().getFluxByName(fluxName).getId());
			}
		}
		return fallbacksIds;
	}
}
