package org.greenblitz.gbEV3.common;

public class RobotClock {
	private static final long initTime = System.nanoTime();
	
	public static long currentTimeNanos() {
		return System.nanoTime() - initTime;
	}
	
	public static long currentTimeMicros() {
		return currentTimeNanos() / 1000;
	}
	
	public static long currentTimeMillis() {
		return currentTimeMicros() / 1000;
	}
	
	public static long currentTimeSeconds() {
		return currentTimeMillis() / 1000;
	}
}
