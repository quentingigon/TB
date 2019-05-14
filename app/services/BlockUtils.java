package services;

public class BlockUtils {

	// TODO comment + more variables

	public static final Integer beginningHour = 8; // inclusive
	public static final Integer EndHour = 23; // exclusive
	public static final Integer activeTime = EndHour - beginningHour;

	// minutes
	public static final Integer blockDuration = 1;
	public static final Integer blockNumber = 60 / blockDuration * activeTime;
}
