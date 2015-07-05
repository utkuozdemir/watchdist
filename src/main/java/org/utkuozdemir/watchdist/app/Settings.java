package org.utkuozdemir.watchdist.app;

import org.utkuozdemir.watchdist.util.DbManager;

public class Settings {
	private static Integer totalWatchesInDay = null;
	private static Integer watchDurationInHours = null;
	private static Integer minWatchesBetweenTwoWatches = null;
	private static Integer firstWatchStartHour = null;
	private static Integer firstWatchStartMinute = null;

	public static int getTotalWatchesInDay() {
		if (totalWatchesInDay == null) totalWatchesInDay = 24 /
				Integer.parseInt(DbManager.getProperty(Constants.KEY_WATCH_DURATION_IN_HOURS));
		return totalWatchesInDay;
	}

	public static int getOneWatchDurationInHours() {
		if (watchDurationInHours == null)
			watchDurationInHours = Integer.parseInt(DbManager.getProperty(Constants.KEY_WATCH_DURATION_IN_HOURS));
		return watchDurationInHours;
	}

	public static int getMinWatchesBetweenTwoWatches() {
		if (minWatchesBetweenTwoWatches == null)
			minWatchesBetweenTwoWatches = Integer.parseInt(DbManager.getProperty(Constants.KEY_WATCHES_BETWEEN_TWO_WATCHES));
		return minWatchesBetweenTwoWatches;
	}

	public static int getFirstWatchStartHour() {
		if (firstWatchStartHour == null)
			firstWatchStartHour = Integer.parseInt(DbManager.getProperty(Constants.KEY_FIRST_WATCH_START_HOUR));
		return firstWatchStartHour;
	}

	public static int getFirstWatchStartMinute() {
		if (firstWatchStartMinute == null)
			firstWatchStartMinute = Integer.parseInt(DbManager.getProperty(Constants.KEY_FIRST_WATCH_START_MINUTE));
		return firstWatchStartMinute;
	}

	public static void invalidateCache() {
		totalWatchesInDay = null;
		watchDurationInHours = null;
		minWatchesBetweenTwoWatches = null;
		firstWatchStartHour = null;
		firstWatchStartMinute = null;
	}
}