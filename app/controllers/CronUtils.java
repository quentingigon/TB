package controllers;

import models.db.Diffuser;
import models.db.Schedule;

public class CronUtils {

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
}
