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
import java.util.HashMap;
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
		Integer teamId = getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_page.render(dataUtils.getAllSchedulesOfTeam(teamId), null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String name) {
		return ok(schedule_update.render(form, new ScheduleData(scheduleRepository.getByName(name)),
			dataUtils.getAllFluxes(), dataUtils.getAllFluxes(), null));
	}

	@With(UserAuthentificationAction.class)
	public Result createView(Http.Request request) {
		Integer teamId = getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_creation.render(form,
			dataUtils.getAllFluxes(), // TODO change
			dataUtils.getAllFluxesOfTeam(teamId),
			null,
			request));
	}

	@With(UserAuthentificationAction.class)
	public Result activateView(String name, Http.Request request) {
		Integer teamId = getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(schedule_activation.render(form, dataUtils.getAllScreensOfTeam(teamId), new ScheduleData(name), null, request));
	}

	// TODO implement on frontend a way to choose an hour for a flux and then use table scheduled_flux to persist the info
	@With(UserAuthentificationAction.class)
	public Result activate(Http.Request request) {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);
		Integer teamId = getTeamIdOfUserByEmail(request.cookie("email").value());

		ScheduleData data = boundForm.get();

		Schedule schedule = scheduleRepository.getByName(data.getName());

		// incorrect name
		if (schedule == null) {
			return badRequest(schedule_page.render(dataUtils.getAllSchedulesOfTeam(teamId), "Schedule does not exist"));
		}
		else {

			RunningSchedule rs = new RunningSchedule(schedule);
			if (runningScheduleRepository.getByScheduleId(schedule.getId()) != null) {
				return badRequest(schedule_page.render(dataUtils.getAllSchedulesOfTeam(teamId), "This schedule is already activated"));
			}
			rs = runningScheduleRepository.add(rs);

			List<Screen> screens = new ArrayList<>();
			for (String screenMac : data.getScreens()) {
				Screen screen = screenRepository.getByMacAddress(screenMac);
				if (screen == null) {
					return badRequest(schedule_page.render(dataUtils.getAllSchedulesOfTeam(teamId), "screen mac address does not exist : " + screenMac));
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
				fluxRepository.getAllFluxIdsOfTeam(teamId), // fallbackfluxes TODO
				getTimeTable(schedule),
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
		Integer teamId = getTeamIdOfUserByEmail(request.cookie("email").value());

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
		Integer teamId = getTeamIdOfUserByEmail(request.cookie("email").value());

		ScheduleData data = boundForm.get();

		if (data.getName().equals("")) {
			return badRequest(schedule_creation.render(form,
				dataUtils.getAllFluxesOfTeam(teamId),
				dataUtils.getAllFluxesOfTeam(teamId),
				"You must enter a name for the schedule",
				request));
		}
		// schedule already exists
		else if (scheduleRepository.getByName(data.getName()) != null) {
			return badRequest(schedule_creation.render(form,
				dataUtils.getAllFluxesOfTeam(teamId),
				dataUtils.getAllFluxesOfTeam(teamId),
				"Schedule name already exists",
				request));
		}
		else {
			Schedule schedule = new Schedule(data.getName());

			for (String fluxName: data.getFluxes()) {
				if (fluxName != null && fluxRepository.getByName(fluxName) == null) {
					return badRequest(schedule_creation.render(form,
						dataUtils.getAllFluxesOfTeam(teamId),
						dataUtils.getAllFluxesOfTeam(teamId),
						"Flux name does not exists",
						request));
				}
				schedule.addToFluxes(fluxRepository.getByName(fluxName).getId());
			}

			for (String fluxName: data.getFallbackFluxes()) {
				if (fluxName != null && fluxRepository.getByName(fluxName) == null) {
					return badRequest(schedule_creation.render(form,
						dataUtils.getAllFluxesOfTeam(teamId),
						dataUtils.getAllFluxesOfTeam(teamId),
						"Flux name does not exists",
						request));
				}
				schedule.addToFallbacks(fluxRepository.getByName(fluxName).getId());
			}

			schedule = scheduleRepository.add(schedule);

			// TODO do it with triggers
			Team team = teamRepository.getById(teamId);
			team.addToSchedules(schedule.getId());
			teamRepository.update(team);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);
		Integer teamId = getTeamIdOfUserByEmail(request.cookie("email").value());

		ScheduleData data = boundForm.get();
		Schedule schedule = scheduleRepository.getByName(data.getName());

		// name is incorrect
		if (schedule == null) {
			return badRequest(schedule_update.render(form,
				new ScheduleData(data.getName()),
				dataUtils.getAllFluxesOfTeam(teamId),
				dataUtils.getAllFluxesOfTeam(teamId),
				"MAC address does not exists"));
		}
		else {
			// do changes to schedule here
			scheduleRepository.update(schedule);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(String name, Http.Request request) {
		Integer teamId = getTeamIdOfUserByEmail(request.cookie("email").value());

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

	private Integer getTeamIdOfUserByEmail(String email) {
		return userRepository
			.getMemberByUserEmail(email)
			.getTeamId();
	}

	private int getBlockNumberOfTime(String time) {

		int hours = Integer.valueOf(time.split(":")[0]);
		int minutes = Integer.valueOf(time.split(":")[1]);

		int hoursToBlock = (hours - beginningHour / activeTime) * blockNumber * blockDuration;

		// TODO warning: only works with blockDuration == 1 so better change it
		return hoursToBlock + minutes;
	}

	// TODO integrate with schedule etc
	private HashMap<Integer, Integer> getTimeTable(Schedule schedule) {

		List<ScheduledFlux> scheduledFluxes = scheduleRepository.getAllScheduledFluxesByScheduleId(schedule.getId());
		Flux lastFlux = new Flux();
		long lastFluxDuration = 0;
		boolean noFluxSent;

		HashMap<Integer, Integer> timetable = new HashMap<>();
		for (int i = 0; i < blockNumber; i++) {

			noFluxSent = true;

			// if duration of last inserted ScheduledFlux is still not finished iterating over
			// we put last flux in the schedule
			if (lastFluxDuration != 0) {
				lastFluxDuration--;
				timetable.put(i, lastFlux.getId());
			}
			else {
				// check if we must insert fluxes at a certain hour
				for (ScheduledFlux sf : scheduledFluxes) {
					// a flux is set to begin at this block
					if (sf.getStartBlock().equals(i)) {
						Flux flux = fluxRepository.getById(sf.getFluxId());
						lastFlux = flux;
						lastFluxDuration = flux.getDuration();
						timetable.put(i, flux.getId());
						noFluxSent = false;
						scheduledFluxes.remove(sf);
						break;
					}
				}

				if (noFluxSent) {
					// if no flux is set at this block
					timetable.put(i, -1);
				}
			}
		}
		return timetable;
	}
}
