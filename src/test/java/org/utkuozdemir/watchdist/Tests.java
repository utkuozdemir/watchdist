package org.utkuozdemir.watchdist;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.utkuozdemir.watchdist.app.Constants;
import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.domain.Watch;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.domain.WatchValue;
import org.utkuozdemir.watchdist.engine.DistributionEngine;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.FileManager;
import org.utkuozdemir.watchdist.util.SaveMode;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Tests {
	public static final int WATCH_DURATION_IN_HOURS = 2;
	public static final int FIRST_WATCH_START_HOUR = 0;
	public static final int FIRST_WATCH_START_MINUTE = 0;
	public static final int WATCHES_BETWEEN_TWO_WATCHES = 2;

	@Before
	public void prepare() {
		DbManager.setDbPath(FileManager.getDatabasePath());
		FileManager.resetDatabase();

		DbManager.setProperty(Constants.KEY_WATCH_DURATION_IN_HOURS,
				String.valueOf(WATCH_DURATION_IN_HOURS));
		DbManager.setProperty(Constants.KEY_FIRST_WATCH_START_HOUR,
				String.valueOf(FIRST_WATCH_START_HOUR));
		DbManager.setProperty(Constants.KEY_WATCHES_BETWEEN_TWO_WATCHES,
				String.valueOf(WATCHES_BETWEEN_TWO_WATCHES));
		DbManager.setProperty(Constants.KEY_FIRST_WATCH_START_MINUTE,
				String.valueOf(FIRST_WATCH_START_MINUTE));
		Settings.invalidateCache();

		Map<Integer, Double> watchValuesMap = new HashMap<>();
		watchValuesMap.put(0, 1.5);
		watchValuesMap.put(1, 2.0);
		watchValuesMap.put(2, 2.0);
		watchValuesMap.put(3, 1.5);
		watchValuesMap.put(4, 1.0);
		watchValuesMap.put(5, 1.0);
		watchValuesMap.put(6, 1.0);
		watchValuesMap.put(7, 1.0);
		watchValuesMap.put(8, 1.0);
		watchValuesMap.put(9, 1.0);
		watchValuesMap.put(10, 1.5);
		watchValuesMap.put(11, 1.5);

		List<WatchValue> watchValues = IntStream
				.range(0, Settings.getTotalWatchesInDay())
				.boxed().map(i -> new WatchValue(i, watchValuesMap.get(i))).collect(toList());
		DbManager.saveWatchValues(watchValues);
	}

	private List<Soldier> createDummySoldiers(int count) {
		return IntStream.range(0, count).boxed().map(i -> {
					Soldier s = new Soldier("Firstname" + i + " " + "Lastname" + i,
							"Duty_" + i, true, false, 4, i + 1, false);
					s.setId(i + 1);
					return s;
				}
		).collect(toList());
	}

	private List<WatchPoint> createDummyWatchPoints(int totalSoldierCount, int maxSoldierCountForOneWatchPoint) {
		int id = 1;
		List<WatchPoint> watchPoints = new ArrayList<>();
		for (int i = 0; i < (totalSoldierCount / maxSoldierCountForOneWatchPoint); i++) {
			WatchPoint wp = new WatchPoint("WatchPoint_" + i, maxSoldierCountForOneWatchPoint, true, id);
			wp.setId(id++);
			watchPoints.add(wp);
		}
		WatchPoint lastWatchPoint = new WatchPoint(
				"WatchPoint_" + (totalSoldierCount / maxSoldierCountForOneWatchPoint),
				maxSoldierCountForOneWatchPoint, true, id);
		lastWatchPoint.setId(id);
		watchPoints.add(lastWatchPoint);
		return watchPoints;
	}

	@Test
	public void testAvailabilitiesSize() {
		int soldierCount = 100;
		createDummySoldiers(soldierCount).forEach(DbManager::saveSoldier);
		assertEquals(DbManager.countAllAvailabilities(), soldierCount * (7 * (24 /
				Integer.parseInt(DbManager.getProperty(Constants.KEY_WATCH_DURATION_IN_HOURS)))));
	}

	@Test
	public void testGapBetweenTwoWatches() {
		List<Soldier> dummySoldiers = createDummySoldiers(10);
		List<WatchPoint> dummyWatchPoints = createDummyWatchPoints(5, 2);
		LocalDate date = LocalDate.of(1989, 9, 19);

		Soldier[][] distribution = DistributionEngine.create(date).distribute(dummySoldiers, dummyWatchPoints);
		Map<Soldier, Set<Integer>> soldierWatchHoursMap = new HashMap<>();
		IntStream.range(0, distribution.length).forEach(i -> Arrays.asList(distribution[i]).
				stream().filter(s -> s != null).
				forEach(soldier -> {
					if (soldierWatchHoursMap.get(soldier) == null) {
						soldierWatchHoursMap.put(soldier, new TreeSet<>());
					}
					soldierWatchHoursMap.get(soldier).add(i);
				}));

		int gap = Integer.parseInt(DbManager.getProperty(Constants.KEY_WATCHES_BETWEEN_TWO_WATCHES));
		List<Soldier> faultySoldiers = new ArrayList<>();
		for (Map.Entry<Soldier, Set<Integer>> entry : soldierWatchHoursMap.entrySet()) {
			Integer[] hours = entry.getValue().toArray(new Integer[entry.getValue().size()]);
			for (int i = 0; i < hours.length - 1; i++) {
				if (hours[i + 1] - hours[i] <= gap) faultySoldiers.add(entry.getKey());
			}
		}
		assertTrue(faultySoldiers.isEmpty());
	}

	@Test
	public void testPreviousDaysWatchesCondition() {
		LocalDate date = LocalDate.of(1989, 9, 19);

		List<Soldier> dummySoldiers = createDummySoldiers(10);
		dummySoldiers.forEach(DbManager::saveSoldier);

		List<WatchPoint> dummyWatchPoints = createDummyWatchPoints(5, 2);
		dummyWatchPoints.forEach(wp -> DbManager.saveWatchPoint(wp, SaveMode.INSERT_OR_UPDATE));

		List<Watch> watches = Arrays.asList(
				new Watch(dummySoldiers.get(0), dummyWatchPoints.get(0), 0, date.toString(), 10, 1.0),
				new Watch(dummySoldiers.get(1), dummyWatchPoints.get(0), 1, date.toString(), 10, 1.0),
				new Watch(dummySoldiers.get(2), dummyWatchPoints.get(1), 0, date.toString(), 10, 1.0),
				new Watch(dummySoldiers.get(3), dummyWatchPoints.get(1), 1, date.toString(), 10, 1.0),
				new Watch(dummySoldiers.get(4), dummyWatchPoints.get(2), 0, date.toString(), 10, 1.0),
				new Watch(dummySoldiers.get(5), dummyWatchPoints.get(0), 0, date.toString(), 11, 1.0),
				new Watch(dummySoldiers.get(6), dummyWatchPoints.get(0), 1, date.toString(), 11, 1.0),
				new Watch(dummySoldiers.get(7), dummyWatchPoints.get(1), 0, date.toString(), 11, 1.0),
				new Watch(dummySoldiers.get(8), dummyWatchPoints.get(1), 1, date.toString(), 11, 1.0),
				new Watch(dummySoldiers.get(9), dummyWatchPoints.get(2), 0, date.toString(), 11, 1.0)
		);
		DbManager.saveWatchesAddPointsDeleteOldWatches(date, watches);
		Soldier[][] distribution = DistributionEngine.create(date.plusDays(1))
				.distribute(dummySoldiers, dummyWatchPoints);

		assertTrue(Arrays.stream(distribution[0]).filter(s -> s != null).count() == 0);
	}

	@Test
	public void testNoSameSoldierSameHour() {
		LocalDate date = LocalDate.of(1989, 9, 19);

		List<Soldier> dummySoldiers = createDummySoldiers(10);
		dummySoldiers.forEach(DbManager::saveSoldier);

		List<WatchPoint> dummyWatchPoints = createDummyWatchPoints(5, 2);
		dummyWatchPoints.forEach(wp -> DbManager.saveWatchPoint(wp, SaveMode.INSERT_OR_UPDATE));

		Soldier[][] distribution = DistributionEngine.create(date).distribute(dummySoldiers, dummyWatchPoints);

		boolean unique = true;
		for (Soldier[] soldiers : distribution) {
			if (Arrays.stream(soldiers).filter(s -> s != null).collect(toSet()).size() !=
					Arrays.stream(soldiers).filter(s -> s != null).collect(toList()).size()) {
				unique = false;
			}
		}
		assertTrue(unique);
	}

	// todo handle big data. optimize distribution engine.
//	@Test
//	public void testBigData() {
//		List<Soldier> dummySoldiers = createDummySoldiers(5000);
//		dummySoldiers.forEach(DbManager::saveSoldier);
//
//		List<WatchPoint> dummyWatchPoints = createDummyWatchPoints(20, 3);
//		dummyWatchPoints.forEach(wp -> DbManager.saveWatchPoint(wp, SaveMode.INSERT_OR_UPDATE));
//
//		LocalDate date = LocalDate.of(2015, 6, 1);
//		for (int i = 0; i < 30; i++) {
//			Soldier[][] distribution = DistributionEngine.create(date.plusDays(i))
//					.distribute(dummySoldiers, dummyWatchPoints);
//
//			Set<Watch> watches = new HashSet<>();
//			for (int j = 0; j < distribution.length; j++) {
//				for (int k = 0; k < distribution[j].length; k++) {
//					Soldier soldier = distribution[j][k];
//					Watch watch = new Watch(soldier, dummyWatchPoints.get((k + 1) / 3), k % 3, date.plusDays(i).toString(), j, WatchValues.get(j));
//					watches.add(watch);
//				}
//			}
//
//			DbManager.saveWatchesAddPointsDeleteOldWatches(date.plusDays(i), watches);
//		}
//	}

	@After
	public void tearDown() {
		DbManager.close();
	}
}
