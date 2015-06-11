package org.utkuozdemir.watchdist.engine;

import org.utkuozdemir.watchdist.Settings;
import org.utkuozdemir.watchdist.domain.Availability;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.domain.Watch;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WatchPointSoldierCalculator;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DistributionEngine {
	private static final int POINTS_EFFECT = 100;
	private static final int AVAILABILITY_EFFECT = 200;

	public static Soldier[][] distribute(LocalDate date, List<Soldier> soldiers, List<WatchPoint> watchPoints) {
		int soldierCountForWatch = watchPoints.stream().mapToInt(WatchPoint::getRequiredSoldierCount).sum();
		Soldier[][] result = new Soldier[Settings.getTotalWatchesInDay()][soldierCountForWatch];

		int maxAssignInDayLimit = 8 / Settings.getOneWatchDurationInHours();

		int maxAssigns = 1;
		boolean hasNull = true;
		while (hasNull && maxAssigns <= maxAssignInDayLimit) {
			Random r = new Random();
			Soldier[][] distribution = initDistributionMatrix(date, watchPoints);

			final int finalMaxAssigns = maxAssigns;
			DbManager.findAllWatchValues()
					.stream().sorted((o1, o2) -> (int) ((o2.getValue() - o1.getValue()) * 1000))
					.forEach(watchValue -> {
						int i = watchValue.getHour() + Settings.getMinWatchesBetweenTwoWatches();
						for (int j = 0; j < soldierCountForWatch; j++) {
							Map<Soldier, Integer> map
									= getSoldierTicketMapForIndex(date, distribution,
									new Index(i, j), soldiers, soldierCountForWatch, finalMaxAssigns);
							List<Soldier> ticketList = new ArrayList<>();
							map.entrySet().stream().forEach(
									e -> IntStream.range(0, e.getValue())
											.forEach(value -> ticketList.add(e.getKey()))
							);

							Soldier pikachu = ticketList.isEmpty() ? null : ticketList.get(r.nextInt(ticketList.size()));

							distribution[i][j] = pikachu;
							result[i - Settings.getMinWatchesBetweenTwoWatches()][j] = pikachu;
						}
					});
			hasNull = Arrays.stream(result).flatMap(Arrays::stream).anyMatch(s -> s == null);
			maxAssigns++;
		}
		return shuffleRows(result);
	}

	private static Soldier[][] initDistributionMatrix(LocalDate date, List<WatchPoint> watchPoints) {
		int soldierCount = WatchPointSoldierCalculator.getTotalWatchPointSoldierCount(watchPoints);
		Soldier[][] distribution = new Soldier[Settings.getTotalWatchesInDay() +
				(2 * Settings.getMinWatchesBetweenTwoWatches())][soldierCount];
		int lineNum = 0;
		for (int i = Settings.getTotalWatchesInDay() - Settings.getMinWatchesBetweenTwoWatches();
			 i < Settings.getTotalWatchesInDay(); i++) {
			List<Watch> watches = DbManager.findWatchesByDateAndHour(date.minusDays(1), i);
			List<Soldier> watchSoldiers = watches.parallelStream().map(Watch::getSoldier).collect(Collectors.toList());
			distribution[lineNum] = watchSoldiers.toArray(new Soldier[watchSoldiers.size()]);
			lineNum++;
		}
		return distribution;
	}

	private static Map<Soldier, Integer> getSoldierTicketMapForIndex(LocalDate date, Soldier[][] distribution,
																	 Index index, List<Soldier> soldiers,
																	 int soldierCountForWatch, int maxAssigns) {
		if (index.getI() < Settings.getMinWatchesBetweenTwoWatches())
			throw new IllegalArgumentException("\"i\" should be equal or higher than " + Settings
					.getMinWatchesBetweenTwoWatches());
		Soldier[][] temp = cloneArray(distribution);
		temp[index.getI()][index.getJ()] = null;

		Set<Soldier> unavailables
				= getUnavailablesForIndex(date, distribution, index, soldiers, soldierCountForWatch, maxAssigns);

		int dayNum = date.getDayOfWeek().getValue() - 1;
		Set<Soldier> availables = soldiers.stream().filter(s -> !unavailables.contains(s)).collect(Collectors.toSet());
		Map<Soldier, Integer> availablesTicketMap = availables.stream().collect(Collectors.toMap(s -> s, s -> {
					// tickets from availability times (inverse proportion)
					long availabilityCountInDay
							= s.getAvailabilities().stream()
							.filter(availability -> availability.getDayNum() == dayNum).count();
					int ticketCountFromAvailability = (int) (AVAILABILITY_EFFECT - (availabilityCountInDay - 1) *
							(AVAILABILITY_EFFECT / (Settings.getTotalWatchesInDay() - 1)));

					// tickets from points (inverse proportion)
					double scale = availables.stream().mapToDouble(Soldier::getPoints).sum();
					double rate = scale != 0 ? s.getPoints() / scale : 0;
					int ticketCountFromPoints = (int) (POINTS_EFFECT - (POINTS_EFFECT * rate));
					return ticketCountFromAvailability + ticketCountFromPoints;
				}
		));

		return Stream.of(unavailables.stream().collect(Collectors.toMap(s -> s, s -> 0)), availablesTicketMap)
				.map(Map::entrySet)
				.flatMap(Collection::stream)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private static Set<Soldier> getUnavailablesForIndex(LocalDate date, Soldier[][] distribution,
														Index index, List<Soldier> soldiers,
														int soldierCountForWatch, int maxAssigns) {
		Set<Soldier> unavailableSoldiers = new HashSet<>();
		unavailableSoldiers.addAll(soldiers.stream().filter(Soldier::isSergeant).collect(Collectors.toSet()));
		unavailableSoldiers.addAll(soldiers.stream().filter(s -> !s.isAvailable()).collect(Collectors.toSet()));

		Soldier[][] withoutPast = new Soldier[Settings.getTotalWatchesInDay()][soldierCountForWatch];
		for (int i = Settings.getMinWatchesBetweenTwoWatches();
			 i < Settings.getTotalWatchesInDay() + Settings.getMinWatchesBetweenTwoWatches(); i++) {
			System.arraycopy(distribution[i], 0, withoutPast[i - Settings.getMinWatchesBetweenTwoWatches()],
					0, soldierCountForWatch);
		}

		List<Soldier> soldiersFlatMap
				= Arrays.stream(withoutPast).flatMap(Arrays::stream)
				.filter(s -> s != null)
				.collect(Collectors.toList());

		unavailableSoldiers.addAll(soldiers.stream()
				.filter(s -> Collections.frequency(soldiersFlatMap, s) >= maxAssigns).collect(Collectors.toSet()));

		IntStream.rangeClosed(
				index.getI() - Settings.getMinWatchesBetweenTwoWatches(),
				index.getI() + Settings.getMinWatchesBetweenTwoWatches()
		).forEach(num -> unavailableSoldiers.addAll(Arrays.asList(distribution[num])));

		unavailableSoldiers.addAll(soldiers.stream().filter(
				s -> !s.getAvailabilities().contains(new Availability(s, date.getDayOfWeek().getValue() - 1,
						index.getI() - Settings.getMinWatchesBetweenTwoWatches()))).collect(Collectors.toSet()));

		Stream<Soldier> todaysFinishedStream = soldiers.stream()
				.filter(s -> s != null)
				.filter(s -> Collections.frequency(soldiersFlatMap, s) >= s.getMaxWatchesPerDay()
				);

		Stream<Soldier> unavailablesStream = unavailableSoldiers.stream()
				.filter(s -> s != null);

		Set<Soldier> unavailables = Stream.concat(todaysFinishedStream, unavailablesStream).collect(Collectors.toSet());
		return soldiers.stream().filter(unavailables::contains).collect(Collectors.toSet());
	}

	private static Soldier[][] shuffleRows(Soldier[][] soldiers) {
		Soldier[][] shuffled = new Soldier[soldiers.length][];
		for (int i = 0; i < soldiers.length; i++) {
			List<Soldier> shuffledRow = new ArrayList<>();
			Collections.addAll(shuffledRow, soldiers[i]);
			Collections.shuffle(shuffledRow);
			shuffled[i] = shuffledRow.toArray(new Soldier[shuffledRow.size()]);
		}
		return shuffled;
	}

	@SuppressWarnings("unchecked")
	private static Soldier[][] cloneArray(Soldier[][] source) {
		Soldier[][] target = new Soldier[source.length][];
		for (int i = 0; i < source.length; i++) {
			target[i] = Arrays.copyOf(source[i], source[i].length);
		}
		return target;
	}

}
