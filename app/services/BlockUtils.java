package services;

/**
 * This class contains all values and functions for the block system.
 */
public class BlockUtils {

	public static final double beginningHour = 8; // inclusive
	public static final double endHour = 23; // exclusive
	public static final double activeTime = endHour - beginningHour;

	// This variable represent the duration of 1 block.
	// The program was created with 1 minute in mind for this duration,
	// but it should not be to complicated to change it to another value.
	public static final double blockDuration = 1; // minutes
	public static final double blockNumber = 60 / blockDuration * activeTime;

	public static int getBlockNumberOfTime(int hours, int minutes) {
		double hoursToBlock = (hours - beginningHour) / activeTime * blockNumber * blockDuration;

		// warning: only works with blockDuration == 1
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
