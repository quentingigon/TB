package services;

public class BlockUtils {

	// TODO comment + more variables

	public static final double beginningHour = 8; // inclusive
	public static final double endHour = 23; // exclusive
	public static final double activeTime = endHour - beginningHour;

	// minutes
	public static final double blockDuration = 1;
	public static final double blockNumber = 60 / blockDuration * activeTime;
}
