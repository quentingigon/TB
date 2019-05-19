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

		int hours = Integer.valueOf(time.split(":")[0]);
		int minutes = Integer.valueOf(time.split(":")[1]);

		return getBlockNumberOfTime(hours, minutes);
	}
}
