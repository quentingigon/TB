package controllers;

import models.db.Diffuser;
import models.db.Schedule;

public class CronUtils {

	public static String getCronCmdSchedule(Schedule schedule, String time, int repeatDuration, String nextTriggerStartHour) {
		return getCronCmd(time, schedule.getDays(), repeatDuration, nextTriggerStartHour);
	}

	public static String getCronCmdDiffuser(Diffuser diffuser, String time, int repeatDuration, String nextTriggerStartHour) {
		return getCronCmd(time, diffuser.getDays(), repeatDuration, nextTriggerStartHour);
	}

	private static String getCronCmd(String time, String days, int repeatDuration, String nextTriggerStartHour) {
		String hours = time.split(":")[0];
		String minutes = time.split(":")[1];

		if (repeatDuration != 0) {
			minutes += "/" + repeatDuration;
			if (!nextTriggerStartHour.equals(""))
				hours = hours + "-" + nextTriggerStartHour;
		}

		StringBuilder cmd = new StringBuilder("0 " + minutes + " " + hours + " ? " + "* ");

		String[] activeDays = days.split(",");
		for (String day: activeDays) {
			cmd.append(day).append(",");
		}
		cmd.deleteCharAt(cmd.length() - 1);

		return cmd.toString();
	}
}
