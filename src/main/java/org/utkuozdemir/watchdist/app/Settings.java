package org.utkuozdemir.watchdist.app;

import org.utkuozdemir.watchdist.util.DbManager;

import java.util.Optional;

public class Settings {
	private static Optional<Integer> totalWatchesInDay = Optional.empty();
	private static Optional<Integer> watchDurationInHours = Optional.empty();
	private static Optional<Integer> minWatchesBetweenTwoWatches = Optional.empty();
	private static Optional<Integer> firstWatchStartHour = Optional.empty();
	private static Optional<Integer> firstWatchStartMinute = Optional.empty();

	public static int getTotalWatchesInDay() {
		return totalWatchesInDay
				.orElse(24 / Integer.parseInt(DbManager.getProperty(Constants.KEY_WATCH_DURATION_IN_HOURS)));
	}

	public static int getOneWatchDurationInHours() {
		return watchDurationInHours
				.orElse(Integer.parseInt(DbManager.getProperty(Constants.KEY_WATCH_DURATION_IN_HOURS)));
	}

	public static int getMinWatchesBetweenTwoWatches() {
		return minWatchesBetweenTwoWatches
				.orElse(Integer.parseInt(DbManager.getProperty(Constants.KEY_WATCHES_BETWEEN_TWO_WATCHES)));
	}

	public static int getFirstWatchStartHour() {
		return firstWatchStartHour
				.orElse(Integer.parseInt(DbManager.getProperty(Constants.KEY_FIRST_WATCH_START_HOUR)));
	}

	public static int getFirstWatchStartMinute() {
		return firstWatchStartMinute
				.orElse(Integer.parseInt(DbManager.getProperty(Constants.KEY_FIRST_WATCH_START_MINUTE)));
	}

	public static void invalidateCache() {
		totalWatchesInDay = Optional.empty();
		watchDurationInHours = Optional.empty();
		minWatchesBetweenTwoWatches = Optional.empty();
		firstWatchStartHour = Optional.empty();
		firstWatchStartMinute = Optional.empty();
	}
}