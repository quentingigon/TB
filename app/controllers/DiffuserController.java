package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.DiffuserData;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
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
import java.util.*;

import static controllers.CronUtils.*;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.impl.matchers.EverythingMatcher.allJobs;
import static services.BlockUtils.getBlockNumberOfTime;

/**
 * This class implements a controller for the Diffusers.
 * It gives CRUD operations and offer means to active/deactivate them
 */
public class DiffuserController extends Controller {

	private final EventSourceController eventSourceController;
	private Form<DiffuserData> form;
	private final ServicePicker servicePicker;
	private final EventManager eventManager;

	@Inject
	public DiffuserController(FormFactory formFactory,
							  ServicePicker servicePicker,
							  EventSourceController eventSourceController,
							  EventManager eventManager) {

		this.eventSourceController = eventSourceController;
		this.servicePicker = servicePicker;
		this.form = formFactory.form(DiffuserData.class);
		this.eventManager = eventManager;
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
	public Result updateView(Http.Request request, String name) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(diffuser_update.render(form,
			new DiffuserData(name),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateViewWithErrorMessage(Http.Request request, String name, String error) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(diffuser_update.render(form,
			new DiffuserData(name),
			servicePicker.getFluxService().getAllFluxesOfTeam(teamId),
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
	public Result activate(Http.Request request) throws SchedulerException {

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

			Set<Integer> screenIds = new HashSet<>();
			List<Screen> screens = new ArrayList<>();

			for (String mac: data.getScreens()) {
				Screen screen = screenService.getScreenByMacAddress(mac);
				if (screen == null) {
					return activateViewWithErrorMessage(data.getName(), request, "Screen MAC address does not exists");
				}

				screenIds.add(screen.getId());
				screens.add(screen);
			}

			Flux diffusedFlux = fluxService.getFluxById(diffuser.getFlux());

			// create new runningDiffuser
			RunningDiffuser rd = new RunningDiffuser(diffuser);
			rd.setDiffuserId(diffuser.getId());
			rd.setScreens(new ArrayList<>(screenIds));

			diffuserService.create(rd);

			List<Screen> screensWithNoRunningSchedule = new ArrayList<>();

			for (Screen screen: screens) {
				Integer runningScheduleId = scheduleService.getRunningScheduleOfScreenById(screen.getId());

				// if no RunningSchedule exists for this screen
				if (runningScheduleId == null) {
					screensWithNoRunningSchedule.add(screen);
				}
			}


			// creates job, trigger and listener for all screens that are not associated to a running schedule
			if (!screensWithNoRunningSchedule.isEmpty()) {
				SendEventJobCreator jobCreator = new SendEventJobCreator(servicePicker, eventManager);
				jobCreator.createJobForDiffuser(diffuser, diffusedFlux, screensWithNoRunningSchedule);
			}

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result deactivate(Http.Request request, String name) {
		DiffuserService diffuserService = servicePicker.getDiffuserService();
		FluxService fluxService = servicePicker.getFluxService();

		Diffuser diffuser = diffuserService.getDiffuserByName(name);

		// incorrect name
		if (diffuser == null) {
			return indexWithErrorMessage(request, "Diffuser name does not exist");
		}
		else {
			RunningDiffuser rd = diffuserService.getRunningDiffuserByDiffuserId(diffuser.getId());

			diffuserService.delete(rd);

			SchedulerFactory sf = new StdSchedulerFactory();
			Scheduler scheduler;

			try {
				scheduler = sf.getScheduler();

				Flux flux = fluxService.getFluxById(diffuser.getFlux());
				String jobName = JOB_NAME_TRIGGER + flux.getName() + "#" + getCronCmdDiffuser(diffuser, diffuser.getTime());
				scheduler.deleteJob(new JobKey(jobName, diffuser.getName()));
			} catch (SchedulerException e) {
				e.printStackTrace();
			}

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

			// set active days
			StringBuilder days = new StringBuilder();
			if (data.getDays() == null) {
				diffuser.setDays(ALL_DAYS);
			}
			else {
				for (String day: data.getDays()) {
					days.append(day).append(",");
				}
				days.deleteCharAt(days.length() - 1);
				diffuser.setDays(days.toString());
			}

			diffuser.setTime(data.getStartTime());
			diffuser.setFlux(fluxService.getFluxByName(data.getFluxName()).getId());

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
			return updateViewWithErrorMessage(request, data.getName(), "Diffuser name does not exists");
		}
		else {

			Result error = checkDataIntegrity(data, "update", request);
			if (error != null) {
				return error;
			}
			diffuser.setFlux(fluxService.getFluxByName(data.getFluxName()).getId());
			diffuser.setName(data.getName());
			diffuser.setTime(data.getStartTime());

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

		if (data.getFluxName() != null) {
			if (fluxService.getFluxByName(data.getFluxName()) ==  null) {
				if (action.equals("create"))
					error = createViewWithErrorMessage(request, "Flux does not exists");
				else if (action.equals("update"))
					error = updateViewWithErrorMessage(request, data.getName(), "Flux does not exists");
			}
		}
		else {
			data.setFluxName("");
		}

		if (data.getStartTime() == null) {
			if (action.equals("create"))
				error = createViewWithErrorMessage(request, "You must enter a start time");
			else if (action.equals("update"))
				error = updateViewWithErrorMessage(request, data.getName(), "You must enter a start time");
		}

		if (data.getScreens() != null) {
			for (String screenMAC: data.getScreens()) {
				if (screenService.getScreenByMacAddress(screenMAC) == null) {
					if (action.equals("create"))
						error = createViewWithErrorMessage(request, "Screen MAC address does not exists");
					else if (action.equals("update"))
						error = updateViewWithErrorMessage(request, data.getName(), "Screen MAC address does not exists");
				}
			}
		}
		else {
			data.setScreens(new ArrayList<>());
		}

		return error;
	}

	private String getScreenIds(List<Screen> screens) {
		StringBuilder output = new StringBuilder();

		for (Screen screen: screens) {
			output.append(screen.getId()).append(",");
		}
		output.deleteCharAt(output.length() - 1);
		return output.toString();
	}

}
