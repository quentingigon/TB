package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.DataUtils;
import models.entities.ScheduleData;
import models.repositories.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.FluxManager;
import services.RunningScheduleService;
import services.RunningScheduleServiceManager;
import views.html.schedule.schedule_activation;
import views.html.schedule.schedule_creation;
import views.html.schedule.schedule_page;
import views.html.schedule.schedule_update;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static services.BlockUtils.*;

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
	TeamRepository teamRepository;

	@Inject
	UserRepository userRepository;

	@Inject
	ScheduleFluxesRepository scheduleFluxesRepository;

	private final FluxManager fluxManager;

	private final RunningScheduleServiceManager serviceManager;

	private Form<ScheduleData> form;

	private DataUtils dataUtils;

	@Inject
	public ScheduleController(FormFactory formFactory,
							  FluxManager fluxManager,
							  RunningScheduleServiceManager serviceManager,
							  DataUtils dataUtils) {
		this.form = formFactory.form(ScheduleData.class);
		this.fluxManager = fluxManager;
		this.serviceManager = serviceManager;
		this.dataUtils = dataUtils;
		Thread t = new Thread(this.fluxManager);
		t.start();
	}

	@With(UserAuthentificationAction.class)
	public Result index(Http.Request request) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_page.render(dataUtils.getAllSchedulesOfTeam(teamId), null));
	}

	private Result indexWithErrorMessage(Http.Request request, String error) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(schedule_page.render(dataUtils.getAllSchedulesOfTeam(teamId), error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(Http.Request request, String name) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_update.render(form, new ScheduleData(scheduleRepository.getByName(name)),
			dataUtils.getAllFluxesOfTeam(teamId), dataUtils.getAllFluxesOfTeam(teamId), null));
	}

	private Result updateViewWithErrorMessage(Http.Request request, String name, String error) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_update.render(form, new ScheduleData(scheduleRepository.getByName(name)),
			dataUtils.getAllFluxesOfTeam(teamId), dataUtils.getAllFluxesOfTeam(teamId), error));
	}

	@With(UserAuthentificationAction.class)
	public Result createView(Http.Request request) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_creation.render(form,
			dataUtils.getAllFluxesOfTeam(teamId),
			dataUtils.getAllFluxesOfTeam(teamId),
			null,
			request));
	}

	private Result createViewWithErrorMessage(Http.Request request, String error) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_creation.render(form,
			dataUtils.getAllFluxesOfTeam(teamId),
			dataUtils.getAllFluxesOfTeam(teamId),
			error,
			request));
	}

	@With(UserAuthentificationAction.class)
	public Result activateView(String name, Http.Request request) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_activation.render(form, dataUtils.getAllScreensOfTeam(teamId), new ScheduleData(name), null, request));
	}

	// TODO implement on frontend a way to choose an hour for a flux and then use table scheduled_flux to persist the info
	@With(UserAuthentificationAction.class)
	public Result activate(Http.Request request) {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());

		ScheduleData data = boundForm.get();

		Schedule schedule = scheduleRepository.getByName(data.getName());

		// incorrect name
		if (schedule == null) {
			return indexWithErrorMessage(request, "Schedule does not exist");
		}
		else {

			// create runningSchedule
			RunningSchedule rs = new RunningSchedule(schedule);
			if (runningScheduleRepository.getByScheduleId(schedule.getId()) != null) {
				return indexWithErrorMessage(request, "This schedule is already activated");
			}
			rs = runningScheduleRepository.add(rs);
			// TODO use trigger to avoid add + merge -> set rs id of screen for each screen of rs at insert

			List<Screen> screens = new ArrayList<>();
			for (String screenMac : data.getScreens()) {
				Screen screen = screenRepository.getByMacAddress(screenMac);
				if (screen == null) {
					return indexWithErrorMessage(request, "screen mac address does not exist : " + screenMac);
				}
				rs.addToScreens(screenRepository.getByMacAddress(screenMac).getId());
				screen.setRunningscheduleId(rs.getId());

				screens.add(screen);
				screenRepository.update(screen);
			}
			runningScheduleRepository.update(rs);

			// add service as observer of FluxManager
			RunningScheduleService service2 = new RunningScheduleService(
				runningScheduleRepository.getByScheduleId(schedule.getId()),
				screens,
				new ArrayList<>(schedule.getFallbacks()),
				dataUtils.getTimeTable(schedule),
				fluxRepository);

			service2.addObserver(fluxManager);

			// the schedule is activated
			serviceManager.addRunningSchedule(schedule.getId(), service2);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result deactivate(String name, Http.Request request) {
		Schedule schedule = scheduleRepository.getByName(name);
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());

		// incorrect name
		if (schedule == null) {
			return badRequest(schedule_page.render(dataUtils.getAllSchedulesOfTeam(teamId), "Schedule does not exist"));
		}
		else {
			RunningSchedule rs = runningScheduleRepository.getByScheduleId(schedule.getId());

			// TODO do it with triggers
			// remove RunningSchedule reference from all concerned screens
			if (rs != null && screenRepository.getAllByRunningScheduleId(rs.getId()) != null) {
				for (Screen s : screenRepository.getAllByRunningScheduleId(rs.getId())) {
					s.setRunningscheduleId(null);
					screenRepository.update(s);
				}
			}

			runningScheduleRepository.delete(rs);

			serviceManager.removeRunningSchedule(schedule.getId());

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());

		ScheduleData data = boundForm.get();

		if (data.getName().equals("")) {
			return createViewWithErrorMessage(request, "You must enter a name for the new schedule");
		}
		// schedule already exists
		else if (scheduleRepository.getByName(data.getName()) != null) {
			return createViewWithErrorMessage(request, "Schedule name already exists");
		}
		else {
			Schedule schedule = new Schedule(data.getName());

			List<ScheduledFlux> scheduledFluxes = new ArrayList<>();

			for (String fluxData: data.getFluxes()) {

				String[] fluxDatas = fluxData.split("#");
				String fluxName = fluxDatas[0];

				if (fluxName == null || fluxRepository.getByName(fluxName) == null) {
					return createViewWithErrorMessage(request, "Flux name does not exists");
				}

				String fluxTime;

				// we must create a ScheduledFlux for this entry
				if (fluxDatas.length == 2 && !fluxDatas[1].equals("")) {
					fluxTime = fluxDatas[1];
					int fluxHour = Integer.valueOf(fluxTime.split((":"))[0]);

					if (fluxHour < beginningHour || fluxHour > endHour) {
						return createViewWithErrorMessage(request,
							"Time for flux: " + fluxName + " is not within bounds: " + beginningHour + " -> " + endHour);
					}

					// TODO set schedule_id of sf with a trigger at the creation of the schedule
					ScheduledFlux sf = new ScheduledFlux();
					sf.setFluxId(fluxRepository.getByName(fluxName).getId());
					sf.setStartBlock(getBlockNumberOfTime(fluxTime));
					scheduledFluxes.add(sf);
					// sf = fluxRepository.addScheduledFlux(sf);

					// schedule.addToFluxes(sf.getId());
				}
				// we must add a un-scheduled flux -> fallback flux
				else {
					schedule.addToFallbacks(fluxRepository.getByName(fluxName).getId());
				}
			}

			schedule = scheduleRepository.add(schedule);

			for (ScheduledFlux sf: scheduledFluxes) {
				sf.setScheduleId(schedule.getId());
				sf = fluxRepository.addScheduledFlux(sf);
				schedule.addToFluxes(sf.getId());
			}
			scheduleRepository.update(schedule);

			// add new schedule to current user's team
			Team team = teamRepository.getById(teamId);
			team.addToSchedules(schedule.getId());
			teamRepository.update(team);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());

		ScheduleData data = boundForm.get();
		Schedule schedule = scheduleRepository.getByName(data.getName());

		// name is incorrect
		if (schedule == null) {
			return updateViewWithErrorMessage(request, data.getName(), "Schedule name does not exists");
		}
		else {
			// do changes to schedule here
			scheduleRepository.update(schedule);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String name, Http.Request request) {
		Integer teamId = dataUtils.getTeamIdOfUserByEmail(request.cookie("email").value());

		Schedule schedule = scheduleRepository.getByName(name);

		// name is incorrect
		if (schedule == null) {
			return badRequest(schedule_page.render(dataUtils.getAllSchedulesOfTeam(teamId), "Name in incorrect"));
		}
		else {
			scheduleRepository.delete(schedule);

			return index(request);
		}
	}

	private int getBlockNumberOfTime(String time) {

		int hours = Integer.valueOf(time.split(":")[0]);
		int minutes = Integer.valueOf(time.split(":")[1]);

		double hoursToBlock = (hours - beginningHour) / activeTime * blockNumber * blockDuration;

		// TODO warning: only works with blockDuration == 1 so better change it
		return (int) hoursToBlock + minutes;
	}



}
