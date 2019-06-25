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
import java.util.*;

import static controllers.CronUtils.getCronCmdSchedule;
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

	private final EventSourceController eventSourceController;
	private Form<ScheduleData> form;
	private final ServicePicker servicePicker;

	@Inject
	public ScheduleController(FormFactory formFactory,
							  ServicePicker servicePicker,
							  EventSourceController eventSourceController) {
		this.form = formFactory.form(ScheduleData.class);
		this.servicePicker = servicePicker;
		this.eventSourceController = eventSourceController;

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

					// check if the schedule to activate overlaps with an other one
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

			// get fallbacks
			for (Integer fluxId: getFallbackFluxIds(data)) {
				schedule.addToFallbacks(fluxId);
			}

			schedule = scheduleService.create(schedule);

			createFluxTriggers(data, schedule);

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

			Result error = checkFluxesIntegrity(data, request);
			if (error != null) {
				return error;
			}

			for (Integer fluxId: getFallbackFluxIds(data)) {
				schedule.addToFallbacks(fluxId);
			}

			createFluxTriggers(data, schedule);

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

		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler scheduler;
		try {
			scheduler = sf.getScheduler();

			List<FluxTrigger> triggers = fluxService.getFluxTriggersOfScheduleById(schedule.getId());

			// run through triggers in DB and schedule job accordingly
			for (FluxTrigger ft: triggers) {
				Flux flux = fluxService.getFluxById(ft.getFluxId());

				JobDetail job = newJob(SendEventJob.class)
					.withIdentity("sendEventJob#" + flux.getName() + "#" + ft.getCronCmd(), schedule.getName())
					.build();

				CronTrigger trigger = newTrigger()
					.withIdentity("trigger#" + flux.getName() + "#" + ft.getCronCmd(), schedule.getName())
					.usingJobData("screenIds", getScreenIds(screens))
					.usingJobData("fluxId", flux.getId())
					.withSchedule(cronSchedule(ft.getCronCmd()))
					.build();
				scheduler.scheduleJob(job, trigger);
			}

			SendEventJobsListener listener = new SendEventJobsListener(
				schedule.getName(),
				eventSourceController,
				servicePicker);
			scheduler.getListenerManager().addJobListener(listener, allJobs());
		} catch (SchedulerException e) {
			e.printStackTrace();
		}


	}

	private void deleteJobsOfSchedule(Schedule schedule) {
		FluxService fluxService = servicePicker.getFluxService();

		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler scheduler;

		try {
			scheduler = sf.getScheduler();
			scheduler.getListenerManager().removeJobListener(schedule.getName());

			List<FluxTrigger> triggers = fluxService.getFluxTriggersOfScheduleById(schedule.getId());

			for (FluxTrigger ft: triggers) {
				Flux flux = fluxService.getFluxById(ft.getFluxId());
				String jobName = "sendEventJob#" + flux.getName() + "#" + ft.getCronCmd();
				scheduler.deleteJob(new JobKey(jobName, schedule.getName()));
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

				// we must create a FluxTrigger for this entry
				if (fluxDatas.length == 2 && !fluxDatas[1].equals("")) {
					String fluxTime = fluxDatas[1];
					int fluxHour = Integer.parseInt(fluxTime.split((":"))[0]);

					// TODO complete
				}
			}
		}
		return error;
	}

	private void createFluxTriggers(ScheduleData data, Schedule schedule) {
		List<FluxTrigger> triggers = getFluxTriggersFromData(data, schedule);
		triggers = setCronCmdForTriggers(triggers);

		// create flux triggers for database
		for (FluxTrigger ft: triggers) {
			ft = servicePicker.getFluxService().createFluxTrigger(ft);
			schedule.addToFluxtriggers(ft.getId());
		}
		servicePicker.getScheduleService().update(schedule);
	}

	private List<FluxTrigger> getFluxTriggersFromData(ScheduleData data, Schedule schedule) {
		List<FluxTrigger> fluxTriggers = new ArrayList<>();

		if (data.getFluxes() != null) {
			for (String fluxData: data.getFluxes()) {

				String fluxName = fluxData.split("#")[0];
				String fluxTime = fluxData.split("#")[1];
				String repeat = fluxData.split("#")[2];

				Flux flux = servicePicker.getFluxService().getFluxByName(fluxName);

				// create trigger without cron command
				fluxTriggers.add(new FluxTrigger(fluxTime, flux.getId(), schedule.getId(), repeat.equals("true")));
			}
		}
		return fluxTriggers;
	}

	private List<FluxTrigger> setCronCmdForTriggers(List<FluxTrigger> triggers) {
		triggers.sort(Comparator.comparing(FluxTrigger::getTime));
		List<FluxTrigger> output = new ArrayList<>();

		for (int i = 0; i < triggers.size(); i++) {
			FluxTrigger ft = triggers.get(i);
			Flux flux = servicePicker.getFluxService().getFluxById(ft.getFluxId());
			Schedule schedule = servicePicker.getScheduleService().getScheduleById(ft.getScheduleId());

			int repeatDuration = 0;
			String nextTriggerStartHour = "";
			if (ft.isRepeat()) {
				repeatDuration = flux.getTotalDuration();

				if (triggers.size() > (i + 1)) {
					nextTriggerStartHour = triggers.get(i + 1).getTime().split(":")[0];
				}
			}

			ft.setCronCmd(getCronCmdSchedule(schedule, ft.getTime(), repeatDuration, nextTriggerStartHour));
			output.add(ft);
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

	private String getScreenIds(List<Screen> screens) {
		StringBuilder output = new StringBuilder();

		for (Screen screen: screens) {
			output.append(screen.getId());
		}
		return output.toString();
	}

	private boolean checkIfSchedulesOverlap(Schedule existingSchedule, Schedule scheduleToActivate) {
		String[] existingDays = existingSchedule.getDays().split(",");
		String[] newDays = scheduleToActivate.getDays().split(",");

		boolean output = false;

		for (String day: newDays) {
			if (Arrays.asList(existingDays).contains(day)) {
				output = true;
			}
		}
		return output;
	}
}
