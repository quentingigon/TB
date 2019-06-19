package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.ScheduleData;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.EverythingMatcher.allJobs;
import static services.BlockUtils.*;

/**
 * This class implements a controller for the Schedules.
 * It gives CRUD operation and offer means to active/deactivate them.
 */
public class ScheduleController extends Controller {

	private final FluxManager fluxManager;
	private final RunningScheduleThreadManager threadManager;
	private final FluxChecker fluxChecker;
	private final EventSourceController eventSourceController;

	private Form<ScheduleData> form;

	private final ServicePicker servicePicker;
	private final TimeTableUtils timeTableUtils;

	@Inject
	public ScheduleController(FormFactory formFactory,
							  FluxManager fluxManager,
							  RunningScheduleThreadManager threadManager,
							  ServicePicker servicePicker,
							  TimeTableUtils timeTableUtils,
							  FluxChecker fluxChecker,
							  EventSourceController eventSourceController) {
		this.fluxChecker = fluxChecker;
		this.timeTableUtils = timeTableUtils;
		this.form = formFactory.form(ScheduleData.class);
		this.fluxManager = fluxManager;
		this.threadManager = threadManager;
		this.servicePicker = servicePicker;
		this.eventSourceController = eventSourceController;
		Thread t = new Thread(this.fluxManager);
		t.start();

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
	public Result activate(Http.Request request) throws SchedulerException {
		final Form<ScheduleData> boundForm = form.bindFromRequest(request);
		ScheduleService scheduleService = servicePicker.getScheduleService();
		ScreenService screenService = servicePicker.getScreenService();
		FluxService fluxService = servicePicker.getFluxService();
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

			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler scheduler = sf.getScheduler();

			List<FluxTrigger> triggers = fluxService.getFluxTriggersOfScheduleById(schedule.getId());

			int index = scheduler.getCurrentlyExecutingJobs().size() + 1;
			// run through triggers in DB and schedule job accordingly
			for (FluxTrigger ft: triggers) {
				Flux flux = fluxService.getFluxById(ft.getFluxId());

				JobDetail job = newJob(SendEventJob.class)
					.withIdentity("sendEventJob" + index++ + " for " + flux.getName(), schedule.getName())
					.build();

				CronTrigger trigger = newTrigger()
					.withIdentity("trigger" + index + " for " + flux.getName(), schedule.getName())
					.usingJobData("screenIds", getScreenIds(screens))
					.usingJobData("fluxId", flux.getId())
					.withSchedule(cronSchedule(ft.getCronCmd()))
					.build();
				scheduler.scheduleJob(job, trigger);
			}

			SendEventJobsListener listener = new SendEventJobsListener(schedule.getName(), eventSourceController, servicePicker);
			scheduler.getListenerManager().addJobListener(listener, allJobs());

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

			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler scheduler;
			try {
				scheduler = sf.getScheduler();
				scheduler.getListenerManager().removeJobListener(schedule.getName());
			} catch (SchedulerException e) {
				e.printStackTrace();
			}

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

			// get fallbacks
			for (Integer fluxId: getFallbackFluxIds(data)) {
				schedule.addToFallbacks(fluxId);
			}

			schedule = scheduleService.create(schedule);

			// create flux triggers for database
			for (FluxTrigger ft: getFluxTriggerFromData(data, schedule)) {
				ft.setScheduleId(schedule.getId());
				servicePicker.getFluxService().createFluxTrigger(ft);
				//schedule.addToFluxTriggers(ft);
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
			for (Integer fluxId: getUnscheduledFluxIds(data)) {
				schedule.addToFluxes(fluxId);
			}

			for (Integer fluxId: getFallbackFluxIds(data)) {
				schedule.addToFallbacks(fluxId);
			}

			// TODO Warning: there are no checks to avoid bad timetable init -> overlapping fluxes, etc
			for (ScheduledFlux sf: getScheduledFluxesFromData(data)) {
				sf.setScheduleId(schedule.getId());
				sf = servicePicker.getFluxService().createScheduled(sf);
				schedule.addToScheduledFluxes(sf.getId());
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

		if (data.getFluxes() != null) {
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
		}

		return error;
	}

	private List<FluxTrigger> getFluxTriggerFromData(ScheduleData data, Schedule schedule) {
		List<FluxTrigger> fluxTriggers = new ArrayList<>();

		if (data.getFluxes() != null) {
			for (String fluxData: data.getFluxes()) {

				String fluxName = fluxData.split("#")[0];
				String fluxTime = fluxData.split("#")[1];

				Integer fluxId = servicePicker.getFluxService().getFluxByName(fluxName).getId();

				// create trigger with cron command
				fluxTriggers.add(new FluxTrigger(fluxId, getCronCmd(schedule, fluxTime)));
			}
		}
		return fluxTriggers;
	}

	private List<ScheduledFlux> getScheduledFluxesFromData(ScheduleData data) {
		List<ScheduledFlux> scheduledFluxes = new ArrayList<>();

		if (data.getFluxes() != null) {
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
		}

		return scheduledFluxes;
	}

	private List<Integer> getUnscheduledFluxIds(ScheduleData data) {

		List<Integer> unscheduledIds = new ArrayList<>();

		if (data.getFluxes() != null) {
			for (String fluxData : data.getFluxes()) {
				String[] fluxDatas = fluxData.split("#");
				String fluxName = fluxDatas[0];

				// we must add a un-scheduled flux -> fallback flux
				if (fluxDatas.length != 2) {
					unscheduledIds.add(servicePicker.getFluxService().getFluxByName(fluxName).getId());
				}
			}
		}

		return unscheduledIds;
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

	private String getCronCmd(Schedule schedule, String time) {
		String hours = time.split(":")[0];
		String minutes = time.split(":")[1];

		StringBuilder cmd = new StringBuilder("0 " + minutes + " " + hours + " ? " + "* ");

		List<String> activeDays = Arrays.asList(schedule.getDays().split(","));
		for (String day: activeDays) {
			cmd.append(day).append(",");
		}
		cmd.deleteCharAt(cmd.length() - 1);

		return cmd.toString();
	}

	private String getScreenIds(List<Screen> screens) {
		StringBuilder output = new StringBuilder();

		for (Screen screen: screens) {
			output.append(screen.getId());
		}
		return output.toString();
	}
}
