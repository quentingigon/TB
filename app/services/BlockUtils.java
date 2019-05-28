package services;

public class BlockUtils {

	// TODO comment + more variables

	public static final double beginningHour = 8; // inclusive
	public static final double endHour = 23; // exclusive
	public static final double activeTime = endHour - beginningHour;

	// minutes
	public static final double blockDuration = 1;
	public static final double blockNumber = 60 / blockDuration * activeTime;

	public static int getBlockNumberOfTime(int hours, int minutes) {
		double hoursToBlock = (hours - beginningHour) / activeTime * blockNumber * blockDuration;

		// TODO warning: only works with blockDuration == 1 so better change it
		return (int) hoursToBlock + minutes;
	}

	public static int getBlockNumberOfTime(String time) {

		int hours = Integer.parseInt(time.split(":")[0]);
		int minutes = Integer.parseInt(time.split(":")[1]);

		return getBlockNumberOfTime(hours, minutes);
	}

	public static String getTimeOfBlockNumber(int blockNumber) {
		int hours = (int) beginningHour + (blockNumber / 60);
		int minutes = (int) (beginningHour*60) + blockNumber - (hours * 60);

		String textHour = (hours < 10 ? "0" : "") + hours;
		String textMinute = (minutes < 10 ? "0" : "") + minutes;

		return textHour + ":" + textMinute;
	}
}
