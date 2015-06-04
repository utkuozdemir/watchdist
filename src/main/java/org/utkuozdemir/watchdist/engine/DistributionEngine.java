package org.utkuozdemir.watchdist.engine;

import org.joda.time.LocalDate;
import org.utkuozdemir.watchdist.domain.Availability;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.domain.Watch;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WatchPointSoldierCalculator;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static org.utkuozdemir.watchdist.Constants.MIN_WATCHES_BETWEEN_TWO_WATCHES;
import static org.utkuozdemir.watchdist.Constants.TOTAL_WATCHES_IN_DAY;

public class DistributionEngine {
	private static final int POINTS_EFFECT = 100;
	private static final int AVAILABILITY_EFFECT = 200;

	private static final int MAX_TRIAL_COUNT = 500;


	public static Soldier[][] distribute(LocalDate date, List<Soldier> soldiers, List<WatchPoint> watchPoints) {
		int soldierCountForWatch = watchPoints.stream().mapToInt(WatchPoint::getRequiredSoldierCount).sum();
		Soldier[][] result = new Soldier[TOTAL_WATCHES_IN_DAY][soldierCountForWatch];

		int trials = 0;
		boolean hasNull = true;
		while (hasNull && trials < MAX_TRIAL_COUNT) {
			Random r = new Random();
			Soldier[][] distribution = initDistributionMatrix(date, watchPoints);

			DbManager.findAllWatchValues()
					.stream().sorted((o1, o2) -> (int) ((o2.getValue() - o1.getValue()) * 1000))
					.forEach(watchValue -> {
						int i = watchValue.getHour() + MIN_WATCHES_BETWEEN_TWO_WATCHES;
						for (int j = 0; j < soldierCountForWatch; j++) {
							Map<Soldier, Integer> map
									= getSoldierTicketMapForIndex(date, distribution, new Index(i, j), soldiers,
									soldierCountForWatch);
							List<Soldier> ticketList = new ArrayList<>();
							map.entrySet().stream().forEach(
									e -> IntStream.range(0, e.getValue())
											.forEach(value -> ticketList.add(e.getKey()))
							);

							Soldier pikachu = ticketList.isEmpty() ? null : ticketList.get(r.nextInt(ticketList.size()));

							distribution[i][j] = pikachu;
							result[i - MIN_WATCHES_BETWEEN_TWO_WATCHES][j] = pikachu;
						}
					});
			hasNull = Arrays.stream(result).flatMap(Arrays::stream).anyMatch(s -> s == null);
			trials++;
		}
		return shuffleRows(result);
	}

	private static Soldier[][] initDistributionMatrix(LocalDate date, List<WatchPoint> watchPoints) {
		int soldierCount = WatchPointSoldierCalculator.getTotalWatchPointSoldierCount(watchPoints);
		Soldier[][] distribution = new Soldier[TOTAL_WATCHES_IN_DAY +
				(2 * MIN_WATCHES_BETWEEN_TWO_WATCHES)][soldierCount];
		int lineNum = 0;
		for (int i = TOTAL_WATCHES_IN_DAY - MIN_WATCHES_BETWEEN_TWO_WATCHES;
			 i < TOTAL_WATCHES_IN_DAY; i++) {
			List<Watch> watches = DbManager.findWatchesByDateAndHour(date.minusDays(1), i);
			List<Soldier> watchSoldiers = watches.parallelStream().map(Watch::getSoldier).collect(Collectors.toList());
			distribution[lineNum] = watchSoldiers.toArray(new Soldier[watchSoldiers.size()]);
			lineNum++;
		}
		return distribution;
	}

	private static Map<Soldier, Integer> getSoldierTicketMapForIndex(LocalDate date, Soldier[][] distribution,
																	 Index index, List<Soldier> soldiers,
																	 int soldierCountForWatch) {
		checkArgument(index.getI() >= MIN_WATCHES_BETWEEN_TWO_WATCHES,
				"\"i\" should be bigger than " + MIN_WATCHES_BETWEEN_TWO_WATCHES);
		Soldier[][] temp = new Soldier[distribution.length][soldierCountForWatch];
		for (int i = 0; i < distribution.length; i++) {
			System.arraycopy(distribution[i], 0, temp[i], 0, distribution[i].length);
		}
		temp[index.getI()][index.getJ()] = null;

		Set<Soldier> unavailables = getUnavailablesForIndex(date, distribution, index, soldiers, soldierCountForWatch);

		int dayNum = date.getDayOfWeek() - 1;
		Set<Soldier> availables = soldiers.stream().filter(s -> !unavailables.contains(s)).collect
				(Collectors.toSet());
		Map<Soldier, Integer> availablesTicketMap = availables.stream().collect(Collectors.toMap(s -> s, s -> {
					// tickets from availability yimes (inverse proportion)
					long availabilityCountInDay
							= s.getAvailabilities().stream()
							.filter(availability -> availability.getDayNum() == dayNum).count();
					int ticketCountFromAvailability = (int) (AVAILABILITY_EFFECT - (availabilityCountInDay - 1) *
							(AVAILABILITY_EFFECT / (TOTAL_WATCHES_IN_DAY - 1)));

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

	private static Set<Soldier> getUnavailablesForIndex(LocalDate date, Soldier[][] distribution, Index index,
														List<Soldier> soldiers, int soldierCountForWatch) {
		Set<Soldier> unavailableSoldiers = new HashSet<>();
		unavailableSoldiers.addAll(soldiers.stream().filter(Soldier::isSergeant).collect(Collectors.toSet()));

		Soldier[][] withoutPast = new Soldier[TOTAL_WATCHES_IN_DAY][soldierCountForWatch];
		for (int i = MIN_WATCHES_BETWEEN_TWO_WATCHES; i < TOTAL_WATCHES_IN_DAY + MIN_WATCHES_BETWEEN_TWO_WATCHES; i++) {
			System.arraycopy(distribution[i], 0, withoutPast[i - MIN_WATCHES_BETWEEN_TWO_WATCHES],
					0, soldierCountForWatch);
		}

		List<Soldier> soldiersFlatMap
				= Arrays.stream(withoutPast).flatMap(Arrays::stream)
				.filter(s -> s != null)
				.collect(Collectors.toList());

		int maxOccurrence
				= soldiersFlatMap.stream()
				.mapToInt(soldier -> Collections.frequency(soldiersFlatMap, soldier)).max().orElse(0);
		if (soldiers.stream().filter(s -> Collections.frequency(soldiersFlatMap, s) < maxOccurrence).count() > 0) {
			unavailableSoldiers.addAll(soldiers.stream().filter(s -> Collections.frequency(soldiersFlatMap, s) >=
					maxOccurrence).collect(Collectors.toSet()));
		}

		IntStream.rangeClosed(
				index.getI() - MIN_WATCHES_BETWEEN_TWO_WATCHES,
				index.getI() + MIN_WATCHES_BETWEEN_TWO_WATCHES
		).forEach(num -> unavailableSoldiers.addAll(Arrays.asList(distribution[num])));

		unavailableSoldiers.addAll(soldiers.stream().filter(
				s -> !s.getAvailabilities().contains(new Availability(s, date.getDayOfWeek() - 1, index.getI()
						- MIN_WATCHES_BETWEEN_TWO_WATCHES))).collect(Collectors.toSet()));

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


}
