package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.ScheduleData;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
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
import java.util.*;

import static controllers.CronUtils.*;

/**
 * This class implements a controller for the Schedules.
 * It gives CRUD operation and offer means to active/deactivate them.
 */
public class ScheduleController extends Controller {

	private Form<ScheduleData> form;
	private final ServicePicker servicePicker;
	private final EventManager eventManager;

	@Inject
	public ScheduleController(FormFactory formFactory,
							  ServicePicker servicePicker,
							  EventManager eventManager) {
		this.form = formFactory.form(ScheduleData.class);
		this.servicePicker = servicePicker;
		this.eventManager = eventManager;

		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler scheduler;
		try {
			scheduler = sf.getScheduler();
			scheduler.start();
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
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
		Schedule schedule = servicePicker.getScheduleService().getScheduleByName(name);
		return ok(schedule_update.render(form,
			new ScheduleData(schedule),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllFluxesOfScheduleById(schedule.getId()),
			null));
	}

	@With(UserAuthentificationAction.class)
	private Result updateViewWithErrorMessage(Http.Request request, String name, String error) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		Schedule schedule = servicePicker.getScheduleService().getScheduleByName(name);
		return ok(schedule_update.render(form,
			new ScheduleData(schedule),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			servicePicker.getFluxService().getAllFluxesOfScheduleById(schedule.getId()),
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

		Schedule scheduleToActivate = scheduleService.getScheduleByName(data.getName());

		// incorrect name
		if (scheduleToActivate == null) {
			return indexWithErrorMessage(request, "Schedule does not exist");
		}
		else {

			// create runningSchedule
			RunningSchedule rs = new RunningSchedule(scheduleToActivate);
			if (scheduleService.getRunningScheduleByScheduleId(scheduleToActivate.getId()) != null) {
				return indexWithErrorMessage(request, "This schedule is already activated");
			}


			List<Screen> screens = new ArrayList<>();
			for (String screenMac : data.getScreens()) {
				Screen screen = screenService.getScreenByMacAddress(screenMac);
				if (screen == null) {
					return indexWithErrorMessage(request, "screen mac address does not exist : " + screenMac);
				}

				Integer runningScheduleId = scheduleService.getRunningScheduleOfScreenById(screen.getId());
				if (runningScheduleId != null) {
					RunningSchedule rs2 = scheduleService.getRunningScheduleById(runningScheduleId);
					Schedule existingSchedule = scheduleService.getScheduleById(rs2.getScheduleId());

					// checkIfFluxLoopMustBeStarted if the schedule to activate overlaps with an other one
					if (existingSchedule == null || checkIfSchedulesOverlap(existingSchedule, scheduleToActivate)) {
						return indexWithErrorMessage(request, "Screen: " + screen.getName() +
							" already has an active schedule for the days chosen");
					}
				}

				rs.addToScreens(screen.getId());

				screens.add(screen);
			}
			rs = scheduleService.create(rs);


			// set RunningSchedule id for screens concerned
			for (Screen screen: screens) {
				screen.setRunningscheduleId(rs.getId());
				screen.setActive(true);
				screenService.update(screen);
			}

			createJobsForSchedule(scheduleToActivate, screens);

			return index(request);
		}
	}



	@With(UserAuthentificationAction.class)
	public Result deactivate(String name, Http.Request request) {
		ScheduleService scheduleService = servicePicker.getScheduleService();
		ScreenService screenService = servicePicker.getScreenService();
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

			for (int screenId: scheduleService.getAllScreenIdsOfRunningScheduleById(rs.getId())) {
				Screen screen = screenService.getScreenById(screenId);
				if (screen != null) {
					screen.setActive(false);
					screenService.update(screen);
				}
			}

			scheduleService.delete(rs);

			deleteJobsOfSchedule(schedule);

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
		} else {
			Schedule schedule = new Schedule(data);

			// set active days
			StringBuilder days = new StringBuilder();
			for (String day: data.getDays()) {
				days.append(day).append(",");
			}
			days.deleteCharAt(days.length() - 1);
			schedule.setDays(days.toString());
			schedule.setStartTime(data.getStartTime());

			// get fallbacks
			for (Integer fluxId: getFallbackFluxIds(data)) {
				schedule.addToFallbacks(fluxId);
			}

			schedule = scheduleService.create(schedule);

			// get boundaries (FluxTrigger) and loops
			List<FluxTrigger> triggers = getFluxTriggersFromData(data, schedule);
			List<FluxLoop> loops = getFluxLoopsFromData(data);
			// loops = getUniqueLoops(loops);

			createFluxTriggersAndFluxLoops(schedule, triggers, loops);

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

			StringBuilder days = new StringBuilder();
			for (String day: data.getDays()) {
				days.append(day).append(",");
			}
			days.deleteCharAt(days.length() - 1);
			schedule.setDays(days.toString());
			schedule.setStartTime(data.getStartTime());

			Result error = checkFluxesIntegrity(data, request);
			if (error != null) {
				return error;
			}

			for (Integer fluxId: getFallbackFluxIds(data)) {
				schedule.addToFallbacks(fluxId);
			}

			List<FluxTrigger> triggers = getFluxTriggersFromData(data, schedule);
			List<FluxLoop> loops = getFluxLoopsFromData(data);
			createFluxTriggersAndFluxLoops(schedule, triggers, loops);

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

	private void createJobsForSchedule(Schedule schedule, List<Screen> screens) {
		FluxService fluxService = servicePicker.getFluxService();

		List<FluxTrigger> triggers = fluxService.getFluxTriggersOfScheduleById(schedule.getId());
		triggers.sort(Comparator.comparing(FluxTrigger::getTime));

		// start correct FluxLoop if needed
		FluxLoop loop = fluxService.getFluxLoopThatMustBeStarted(triggers, schedule);
		if (loop != null) {
			createAndScheduleLoopJob(schedule, screens, loop);
		}

		// run through triggers in DB and schedule job accordingly
		for (FluxTrigger ft: triggers) {
			createAndScheduleJobFromFluxTrigger(schedule, ft, screens);
		}
	}

	private void createAndScheduleLoopJob(Schedule schedule,
										  List<Screen> screens,
										  FluxLoop loop) {
		LoopEventJobCreator loopJobCreator = new LoopEventJobCreator(schedule,
			getScreenIds(screens),
			servicePicker,
			eventManager);
		loopJobCreator.createFromFluxLoop(loop);
	}

	private void createAndScheduleJobFromFluxTrigger(Schedule schedule,
													 FluxTrigger fluxTrigger,
													 List<Screen> screens) {
		SendEventJobCreator jobCreator = new SendEventJobCreator(servicePicker, eventManager);
		jobCreator.createJobForSchedule(schedule, fluxTrigger, screens);
	}

	private void deleteJobsOfSchedule(Schedule schedule) {
		FluxService fluxService = servicePicker.getFluxService();

		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler scheduler;

		try {
			scheduler = sf.getScheduler();

			List<FluxTrigger> triggers = fluxService.getFluxTriggersOfScheduleById(schedule.getId());

			// flux triggers
			for (FluxTrigger ft: triggers) {
				Flux flux = fluxService.getFluxById(ft.getFluxId());
				String jobName = JOB_NAME_TRIGGER + flux.getName() + "#" + getCronCmdSchedule(schedule, ft.getTime());
				scheduler.deleteJob(new JobKey(jobName, SEND_EVENT_GROUP + "." + schedule.getName()));
			}

			// flux loop
			if (scheduler.checkExists(new JobKey(JOB_NAME_LOOP+schedule.getName(), SEND_LOOP_EVENT_GROUP))) {
				scheduler.deleteJob(new JobKey(JOB_NAME_LOOP+schedule.getName(), SEND_LOOP_EVENT_GROUP));
			}

		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	private Result checkFluxesIntegrity(ScheduleData data, Http.Request request) {
		Result error = null;

		if (data.getFluxes() != null) {
			for (String fluxData: data.getFluxes()) {

				String[] fluxDatas = fluxData.split("#");
				String fluxName = fluxDatas[0];

				if (fluxName == null || servicePicker.getFluxService().getFluxByName(fluxName) == null) {
					error = createViewWithErrorMessage(request, "Flux name does not exists");
				}
			}
		}
		return error;
	}

	private void createFluxTriggersAndFluxLoops(Schedule schedule,
												List<FluxTrigger> triggers, List<FluxLoop> loops) {

		// create flux triggers for database
		for (FluxTrigger ft: triggers) {
			ft.setScheduleId(schedule.getId());
			ft = servicePicker.getFluxService().createFluxTrigger(ft);
			schedule.addToFluxtriggers(ft.getId());
		}

		for (FluxLoop loop: loops) {
			loop.setScheduleId(schedule.getId());

			// get flux ids (they are still in the correct order)
			List<Integer> fluxIds = new ArrayList<>(loop.getFluxes());
			loop.setFluxes(new HashSet<>());

			loop = servicePicker.getFluxService().createFluxLoop(loop);

			int order = 1;
			// creates a entry in looped_flux table with correct order
			for (Integer fluxId: fluxIds) {
				servicePicker.getFluxService().addFluxToFluxLoop(loop.getId(), fluxId, order++);
			}

			schedule.addToFluxloops(loop.getId());
		}
		servicePicker.getScheduleService().update(schedule);
	}

	// remove FluxLoops with same fluxes (even if in different order)
	private List<FluxLoop> getUniqueLoops(List<FluxLoop> loops) {
		Set<FluxLoop> output = new HashSet<>(loops);

		for (int i = 0; i < loops.size() - 1; i++) {
			FluxLoop loop1 = loops.get(i);

			for (int j = i + 1; j < loops.size(); j++) {
				FluxLoop loop2 = loops.get(j);

				if (loop1.getFluxes().equals(loop2.getFluxes())) {
					output.remove(loop2);
				}
			}
		}
		return new ArrayList<>(output);
	}

	private List<FluxTrigger> getFluxTriggersFromData(ScheduleData data, Schedule schedule) {
		List<FluxTrigger> fluxTriggers = new ArrayList<>();

		if (data.getFluxes() != null) {
			for (String fluxData: data.getFluxes()) {

				String fluxName = fluxData.split("#")[0];
				String fluxTime = fluxData.split("#")[1];

				Flux flux = servicePicker.getFluxService().getFluxByName(fluxName);

				// if a time was chosen
				if (!fluxTime.equals("")) {
					// create trigger without cron command
					fluxTriggers.add(new FluxTrigger(fluxTime, flux.getId(), schedule.getId()));
				}
			}
		}
		return fluxTriggers;
	}

	private List<FluxLoop> getFluxLoopsFromData(ScheduleData data) {
		List<FluxLoop> loops = new ArrayList<>();

		if (data.getFluxes() != null) {
			boolean grouped = false;
			List<Integer> fluxIds = new ArrayList<>();
			String startTime = data.getStartTime();

			int index = 0;

			for (String fluxData: data.getFluxes()) {

				index++;

				String fluxName = fluxData.split("#")[0];
				String fluxTime = fluxData.split("#")[1];

				Flux flux = servicePicker.getFluxService().getFluxByName(fluxName);

				if (fluxTime.equals("")) {
					fluxIds.add(flux.getId());
					grouped = true;

					if (data.getFluxes().size() == index) {
						FluxLoop loop = new FluxLoop();

						Set<Integer> fluxes = getFluxIdsInOrderFromList(fluxIds);
						loop.setFluxes(fluxes);

						loop.setStartTime(startTime);
						loops.add(loop);
						fluxIds = new ArrayList<>();
					}
				}
				// if we ran through a trigger or all entries have been handled and there are no triggers
				else if (grouped) {
					FluxLoop loop = new FluxLoop();
					loop.setStartTime(startTime);

					Set<Integer> fluxes = getFluxIdsInOrderFromList(fluxIds);
					loop.setFluxes(fluxes);

					loops.add(loop);
					DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
					LocalTime actualTime = formatter.parseLocalTime(fluxTime)
						.plusMinutes(flux.getTotalDuration());
					startTime = formatter.print(actualTime);
					fluxIds = new ArrayList<>();
					grouped = false;
				}
				else {
					DateTimeFormatter formatter = DateTimeFormat.forPattern("HH:mm");
					LocalTime actualTime = formatter.parseLocalTime(fluxTime)
						.plusMinutes(flux.getTotalDuration());
					startTime = formatter.print(actualTime) ;
				}
			}
		}
		return loops;
	}

	private Set<Integer> getFluxIdsInOrderFromList(List<Integer> fluxIds) {
		Set<Integer> output = new LinkedHashSet<>();

		for (Integer fluxId: fluxIds) {
			output.add(fluxId);
		}
		return output;
	}

	private List<Integer> getFallbackFluxIds(ScheduleData data) {

		List<Integer> fallbacksIds = new ArrayList<>();

		if (data.getFallbackFluxes() != null) {
			for (String fluxData : data.getFallbackFluxes()) {
				String[] fluxDatas = fluxData.split("#");
				String fluxName = fluxDatas[0];

				fallbacksIds.add(servicePicker.getFluxService().getFluxByName(fluxName).getId());
			}
		}

		return fallbacksIds;
	}
}
