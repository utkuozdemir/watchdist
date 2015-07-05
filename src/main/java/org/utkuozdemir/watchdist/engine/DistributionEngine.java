package org.utkuozdemir.watchdist.engine;

import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.*;
import org.utkuozdemir.watchdist.util.DbManager;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class DistributionEngine {
	private static final int POINTS_EFFECT = 100;
	private static final int AVAILABILITY_EFFECT = 200;

	private static volatile DistributionEngine INSTANCE;

	private DistributionEngine() {
	}

	public static DistributionEngine getInstance() {
		if (INSTANCE == null) {
			synchronized (DistributionEngine.class) {
				//double checking Singleton instance
				if (INSTANCE == null) {
					INSTANCE = new DistributionEngine();
				}
			}
		}
		return INSTANCE;
	}

	public Soldier[][] distribute(LocalDate date, List<Soldier> soldiers, List<WatchPoint> watchPoints) {
		int soldierCountForWatch = watchPoints.stream().mapToInt(WatchPoint::getRequiredSoldierCount).sum();
		Soldier[][] result = new Soldier[Settings.getTotalWatchesInDay()][soldierCountForWatch];

		int maxAssignInDayLimit = 8 / Settings.getOneWatchDurationInHours();

		int maxAssigns = 1;
		boolean hasNull = true;
		while (hasNull && maxAssigns <= maxAssignInDayLimit) {
			Random r = new Random();
			Soldier[][] distribution = initDistributionMatrix(date, watchPoints);

			final int finalMaxAssigns = maxAssigns;
			Arrays.stream(getOrderedWatchValues(date, soldiers)).forEach(hour -> {
				int i = hour + Settings.getMinWatchesBetweenTwoWatches();
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

	private int[] getOrderedWatchValues(LocalDate date, Collection<Soldier> soldiers) {
		return DbManager.findAllWatchValues()
				.stream().sorted((o1, o2) -> {
					long o1Fixed = soldiers.stream().filter(Soldier::isFixedWatch)
							.filter(s -> s.getAvailabilities().contains(
									new Availability(s, date.getDayOfWeek().getValue() - 1, o1.getHour()))).count();
					long o2Fixed = soldiers.stream().filter(Soldier::isFixedWatch)
							.filter(s -> s.getAvailabilities().contains(
									new Availability(s, date.getDayOfWeek().getValue() - 1, o2.getHour()))).count();
					if (o1Fixed > o2Fixed) return (int) o1Fixed;
					else if (o2Fixed > o1Fixed) return (int) o2Fixed;
					else return (int) ((o2.getValue() - o1.getValue()) * 1000);
				}).mapToInt(WatchValue::getHour).toArray();

	}

	private Soldier[][] initDistributionMatrix(LocalDate date, List<WatchPoint> watchPoints) {
		int soldierCount = watchPoints.stream().mapToInt(WatchPoint::getRequiredSoldierCount).sum();
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

	private Map<Soldier, Integer> getSoldierTicketMapForIndex(LocalDate date, Soldier[][] distribution,
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

	private Set<Soldier> getUnavailablesForIndex(LocalDate date, Soldier[][] distribution,
														Index index, List<Soldier> soldiers,
														int soldierCountForWatch, int maxAssigns) {
		Set<Soldier> availables = new HashSet<>(soldiers);
		availables.removeAll(soldiers.stream().filter(Soldier::isSergeant).collect(Collectors.toSet()));
		availables.removeAll(soldiers.stream().filter(s -> !s.isAvailable()).collect(Collectors.toSet()));

		Set<Soldier> unusedFixedAtThatHour = availables.stream().filter(Soldier::isFixedWatch)
				.filter(s -> s != null)
				.filter(s -> s.getAvailabilities()
						.contains(new Availability(s, date.getDayOfWeek().getValue() - 1,
								index.getI() - Settings.getMinWatchesBetweenTwoWatches())))
				.filter(s -> !Arrays.asList(distribution[index.getI()]).contains(s))
				.collect(Collectors.toSet());

		if (!unusedFixedAtThatHour.isEmpty()) {
			HashSet<Soldier> availablesCopy = new HashSet<>(availables);
			availablesCopy.removeAll(unusedFixedAtThatHour);
			availables.removeAll(availablesCopy);
		}

		List<Soldier> soldiersFlatMap = new ArrayList<>();
		for (int i = Settings.getMinWatchesBetweenTwoWatches();
			 i < Settings.getTotalWatchesInDay() + Settings.getMinWatchesBetweenTwoWatches(); i++) {
			for (int j = 0; j < soldierCountForWatch; j++) {
				if (distribution[i][j] != null) soldiersFlatMap.add(distribution[i][j]);
			}
		}

		availables.removeAll(availables.stream()
				.filter(s -> Collections.frequency(soldiersFlatMap, s) >= maxAssigns).collect(Collectors.toSet()));

		IntStream.rangeClosed(
				index.getI() - Settings.getMinWatchesBetweenTwoWatches(),
				index.getI() + Settings.getMinWatchesBetweenTwoWatches()
		).forEach(num -> availables.removeAll(Arrays.asList(distribution[num])));

		availables.removeAll(availables.stream().filter(
				s -> !s.getAvailabilities().contains(new Availability(s, date.getDayOfWeek().getValue() - 1,
						index.getI() - Settings.getMinWatchesBetweenTwoWatches()))).collect(Collectors.toSet()));

		// todays finished soldiers
		availables.removeAll(soldiers.stream()
				.filter(s -> s != null)
				.filter(s -> Collections.frequency(soldiersFlatMap, s) >= s.getMaxWatchesPerDay()
				).collect(Collectors.toList()));

		return soldiers.stream().filter(s -> s != null && !availables.contains(s)).collect(Collectors.toSet());
	}

	private Soldier[][] shuffleRows(Soldier[][] soldiers) {
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
	private Soldier[][] cloneArray(Soldier[][] source) {
		Soldier[][] target = new Soldier[source.length][];
		for (int i = 0; i < source.length; i++) {
			target[i] = Arrays.copyOf(source[i], source[i].length);
		}
		return target;
	}

}
