package controllers;

import models.db.*;
import services.EventManager;
import services.FluxChecker;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class CronUtils {

	public static final String SCHEDULE_JOBS_LISTENER = "schedulesListener";
	public static final String DIFFUSER_JOBS_LISTENER = "diffusersListener";
	public static final String SCHEDULE_LOOP_JOBS_LISTENER = "schedulesLoopListener";

	public static final String SEND_EVENT_GROUP = "sendEventGroup";
	public static final String SEND_LOOP_EVENT_GROUP = "sendLoopEventGroup";

	public static final String JOB_NAME_LOOP = "sendLoopEventJob#";
	public static final String JOB_NAME_TRIGGER = "sendEventJob#";

	public static final String TRIGGER_NAME = "trigger#";
	public static final String TRIGGER_NAME_LOOP = "triggerLoop#";


	public static final int startHour = 8;
	public static final int endHour = 23;
	public static final String startTime = "08:00";

	private CronUtils() {}

	public static String getCronCmdLoop(String days, String time) {
		return getCronCmd(time, days);
	}

	public static String getCronCmdSchedule(Schedule schedule, String time) {
		return getCronCmd(time, schedule.getDays());
	}

	public static String getCronCmdDiffuser(Diffuser diffuser, String time) {
		return getCronCmd(time, diffuser.getDays());
	}

	private static String getCronCmd(String time, String days) {
		String hours = time.split(":")[0];
		String minutes = time.split(":")[1];

		StringBuilder cmd = new StringBuilder("0 " + minutes + " " + hours + " ? " + "* ");

		String[] activeDays = days.split(",");
		for (String day: activeDays) {
			cmd.append(day).append(",");
		}
		cmd.deleteCharAt(cmd.length() - 1);

		return cmd.toString();
	}

	public static String getScreenIds(List<Screen> screens) {
		StringBuilder output = new StringBuilder();

		for (Screen screen: screens) {
			output.append(screen.getId()).append(",");
		}
		output.deleteCharAt(output.length() - 1);
		return output.toString();
	}

	public static String getFluxIds(List<Integer> fluxes) {
		StringBuilder output = new StringBuilder();

		for (Integer fluxId: fluxes) {
			output.append(fluxId).append(",");
		}
		output.deleteCharAt(output.length() - 1);
		return output.toString();
	}

	public static String getNextFluxTriggerTimeOfSchedule(List<FluxTrigger> triggers, String time) {
		triggers.sort(Comparator.comparing(FluxTrigger::getTime));

		for (FluxTrigger ft : triggers) {
			if (ft.getTime().compareTo(time) > 0) {
				return ft.getTime();
			}
		}
		return "";
	}

	public static boolean mustFluxLoopBeStarted(String currentTime, FluxLoop loop, List<FluxTrigger> triggers) {
		return loop.getStartTime().compareTo(currentTime) == 0 ||
			(loop.getStartTime().compareTo(currentTime) < 0
				&& getNextFluxTriggerTimeOfSchedule(triggers, currentTime).equals("")) ||
			(loop.getStartTime().compareTo(currentTime) < 0
				&& getNextFluxTriggerTimeOfSchedule(triggers, currentTime).compareTo(currentTime) > 0);
	}

	public static boolean checkIfScheduleAndDiffuserDaysOverlap(Schedule schedule, Diffuser diffuser) {
		String[] existingDays = schedule.getDays().split(",");
		String[] newDays = diffuser.getDays().split(",");

		return checkIfDaysOverlap(existingDays, newDays);
	}

	public static boolean checkIfSchedulesOverlap(Schedule existingSchedule, Schedule scheduleToActivate) {
		String[] existingDays = existingSchedule.getDays().split(",");
		String[] newDays = scheduleToActivate.getDays().split(",");

		return checkIfDaysOverlap(existingDays, newDays);
	}

	private static boolean checkIfDaysOverlap(String[] existingDays, String[] newDays) {
		boolean output = false;

		for (String day: newDays) {
			if (Arrays.asList(existingDays).contains(day)) {
				output = true;
			}
		}
		return output;
	}

	// return always true for now
	public static boolean checkIfFluxHasSomethingToDisplay(EventManager eventManager, Flux flux) {

		boolean output = true;
		FluxChecker fluxChecker = eventManager.getFluxChecker();

		// output = !fluxChecker.checkIfFluxHasSomethingToDisplayByDateTime(flux);

		return output;
	}
}
