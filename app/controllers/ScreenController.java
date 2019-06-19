package controllers;

import controllers.actions.UserAuthentificationAction;
import models.db.*;
import models.entities.ScreenData;
import models.repositories.interfaces.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.With;
import services.*;
import views.html.eventsource;
import views.html.screen.screen_code;
import views.html.screen.screen_creation;
import views.html.screen.screen_page;
import views.html.screen.screen_update;

import javax.inject.Inject;
import java.util.*;

import static services.RunningScheduleUtils.NO_SCHEDULE;

/**
 * This class implements a controller for the Screens.
 * It gives CRUD operation and offer means to deactivate an active Screen.
 */
public class ScreenController extends Controller {

	@Inject
	SiteRepository siteRepository;

	private Form<ScreenData> form;

	private final RunningScheduleThreadManager threadManager;
	private final ServicePicker servicePicker;
	private final FluxChecker fluxChecker;
	private final FluxManager fluxManager;

	@Inject
	public ScreenController(FormFactory formFactory,
							RunningScheduleThreadManager threadManager,
							ServicePicker servicePicker,
							FluxChecker fluxChecker,
							FluxManager fluxManager) {
		this.fluxManager = fluxManager;
		this.fluxChecker = fluxChecker;
		this.servicePicker = servicePicker;
		this.threadManager = threadManager;
		this.form = formFactory.form(ScreenData.class);
	}

	@With(UserAuthentificationAction.class)
	public Result index(Http.Request request) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return ok(screen_page.render(servicePicker.getScreenService().getAllScreensOfTeam(teamId),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result indexWithErrorMessage(Http.Request request, String error) {
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		return badRequest(screen_page.render(servicePicker.getScreenService().getAllScreensOfTeam(teamId),
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result createView() {
		return ok(screen_creation.render(form,
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result createViewWithErrorMessage(String error) {
		return badRequest(screen_creation.render(form,
			error));
	}

	@With(UserAuthentificationAction.class)
	public Result updateView(String mac) {
		return ok(screen_update.render(form,
			new ScreenData(servicePicker.getScreenService().getScreenByMacAddress(mac)),
			null));
	}

	@With(UserAuthentificationAction.class)
	public Result updateViewWithErrorMessage(String mac, String error) {
		return badRequest(screen_update.render(form,
			new ScreenData(servicePicker.getScreenService().getScreenByMacAddress(mac)),
			error));
	}

	public Result authentification(Http.Request request) {

		String macAdr = request.queryString().get("mac")[0];
		ScreenService screenService = servicePicker.getScreenService();
		ScheduleService scheduleService = servicePicker.getScheduleService();

		Screen screen = screenService.getScreenByMacAddress(macAdr);

		// screen not registered
		if (screen == null) {

			// if screen already asked for a code
			if (screenService.getWSByMacAddress(macAdr) != null) {
				return ok(screen_code.render(screenService.getWSByMacAddress(macAdr).getCode()));
			}

			String code = screenRegisterCodeGenerator();

			screenService.createWS(new WaitingScreen(code, macAdr));

			// send code
			return ok(screen_code.render(code));
		}
		// screen registered
		else {

			// no active schedules for this screen
			if (scheduleService.getRunningScheduleOfScreenById(screen.getId()) == null) {
				return redirect(routes.ErrorPageController.noScheduleView());
			}

			// Timer task used to force a resend of current flux for the screen
			/*TimerTask task = new TimerTask() {
				public void run() {

					RunningSchedule rs = scheduleService.getRunningScheduleById(
						scheduleService.getRunningScheduleOfScreenById(screen.getId())
					);
					if (rs != null) {
						RunningScheduleThread rst = threadManager.getServiceByScheduleId(rs.getScheduleId());
						List<Screen> screenList = new ArrayList<>();
						screenList.add(screen);
						System.out.println("FORCE SEND");
						rst.resendLastFluxEventToScreens(screenList);
					}

				}
			};
			Timer timer = new Timer("Timer");

			long delay = 2000L;
			timer.schedule(task, delay);

*/
			// screen already logged in
			if (!screen.isLogged()) {
				screen.setLogged(true);

				screenService.update(screen);
			}
			return ok(eventsource.render()).withCookies(
				Http.Cookie.builder("mac", macAdr)
					.withHttpOnly(false)
					.build(),
				Http.Cookie.builder("resolution", screen.getResolution())
					.withHttpOnly(false)
					.build());
		}
	}

	@With(UserAuthentificationAction.class)
	public Result create(Http.Request request) {
		final Form<ScreenData> boundForm = form.bindFromRequest(request);
		Integer teamId = servicePicker.getUserService().getTeamIdOfUserByEmail(request.cookie("email").value());
		ScreenService screenService = servicePicker.getScreenService();
		TeamService teamService = servicePicker.getTeamService();

		ScreenData data = boundForm.get();
		String macAdr = data.getMac();

		// screen is already known
		if (screenService.getScreenByMacAddress(macAdr) != null) {
			return createViewWithErrorMessage("Screen already exists");
		}
		else if (data.getCode() == null) {
			return createViewWithErrorMessage("You must enter a registration code");
		}
		else {
			String code = data.getCode();
			WaitingScreen ws = screenService.getWSByMacAddress(macAdr);

			if (ws == null) {
				return createViewWithErrorMessage(
					"You must first get a registration code by going to this address: /auth?mac=<YourMacAddress>");
			}

			// if macs are the same and code is correct  -> add screen to DB
			if (ws.getMacAddress().equals(data.getMac()) && ws.getCode().equals(code)) {

				Screen newScreen = new Screen(macAdr);

				if (siteRepository.getByName(data.getSite().toLowerCase()) == null) {
					return createViewWithErrorMessage("Bad site name");
				}
				newScreen.setSiteId(siteRepository.getByName(data.getSite().toLowerCase()).getId());
				newScreen.setResolution(data.getResolution());
				newScreen.setLogged(false);
				newScreen.setName(data.getName());

				screenService.create(newScreen);
				screenService.delete(ws);

				// add new schedule to current user's team
				Team team = teamService.getTeamById(teamId);
				team.addScreen(newScreen.getId());
				teamService.update(team);

				return index(request);
			}
			else {
				// wrong code
				return createView();
			}
		}
	}

	@With(UserAuthentificationAction.class)
	public Result update(Http.Request request) {
		final Form<ScreenData> boundForm = form.bindFromRequest(request);
		ScreenService screenService = servicePicker.getScreenService();

		ScreenData data = boundForm.get();

		Screen screen = screenService.getScreenByMacAddress(data.getMac());

		if (screen == null) {
			// screen is not known
			return updateViewWithErrorMessage(data.getMac(), "MAC address does not exists");
		}
		else {
			// update screen
			if (siteRepository.getByName(data.getSite().toLowerCase()) == null) {
				return updateViewWithErrorMessage(data.getMac(), "Bad site name");
			}
			screen.setSiteId(siteRepository.getByName(data.getSite().toLowerCase()).getId());

			screen.setName(data.getName());
			screen.setMacAddress(data.getMac());
			screen.setResolution(data.getResolution());

			screenService.update(screen);

			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result delete(Http.Request request, String mac) {
		Screen screen = servicePicker.getScreenService().getScreenByMacAddress(mac);

		if (screen == null) {
			return indexWithErrorMessage(request, "MAC address does not exists");
		}
		else {
			servicePicker.getScreenService().delete(screen);
			return index(request);
		}
	}

	@With(UserAuthentificationAction.class)
	public Result deactivate(Http.Request request, String mac) {
		Screen screen = servicePicker.getScreenService().getScreenByMacAddress(mac);
		ScheduleService scheduleService = servicePicker.getScheduleService();
		ScreenService screenService = servicePicker.getScreenService();
		FluxService fluxService = servicePicker.getFluxService();

		if (screen == null) {
			return indexWithErrorMessage(request, "MAC address does not exists");
		}
		else {

			Integer rsId = scheduleService.getRunningScheduleOfScreenById(screen.getId());

			if (rsId == null) {
				return redirect(routes.HomeController.index());
			}

			RunningSchedule rs = scheduleService.getRunningScheduleById(rsId);

			// screen is active
			if (rs != null) {
				List<Integer> screenIds = scheduleService.getAllScreenIdsOfRunningScheduleById(rs.getId());
				screenIds.remove(screen.getId());
				rs.setScreens(screenIds);

				RunningScheduleThread rst = threadManager.getServiceByScheduleId(rs.getScheduleId());
				rst.removeFromScreens(screen);

				List<Screen> screenList =new ArrayList<>();
				for (Integer screenId: screenIds) {
					screenList.add(screenService.getScreenById(screenId));
				}

				// stop old thread
				rst.abort();
				threadManager.removeRunningSchedule(rs.getScheduleId());

				Schedule schedule = scheduleService.getScheduleById(rs.getScheduleId());

				// create new thread with values from old thread (timetable especially) and start it
				RunningScheduleThread task = new RunningScheduleThread(
					rs,
					screenList,
					new ArrayList<>(schedule.getFluxes()),
					rst.getTimetable(),
					servicePicker,
					fluxChecker,
					schedule.isKeepOrder());

				task.addObserver(fluxManager);
				threadManager.addRunningScheduleThread(rs.getScheduleId(), task);

			}
			scheduleService.update(rs);
		}
		return index(request);
	}

	private String screenRegisterCodeGenerator() {
		UUID uniqueKey = UUID.randomUUID();

		return uniqueKey.toString().substring(0, 5);
	}
}
