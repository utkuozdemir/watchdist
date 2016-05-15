package org.utkuozdemir.watchdist.engine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.Availability;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.domain.Watch;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.domain.WatchValue;
import org.utkuozdemir.watchdist.util.DbManager;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class DistributionEngine {
    private static final Logger logger = LoggerFactory.getLogger(DistributionEngine.class);

    private static final int POINTS_EFFECT = 100;
    private static final int AVAILABILITY_EFFECT = 200;

    private final LocalDate date;
    private final List<WatchValue> watchValues;
    private final List<Watch> previousDaysWatches;

    private DistributionEngine(LocalDate date) {
        this.date = date;
        watchValues = DbManager.findAllWatchValues();
        previousDaysWatches = DbManager.findWatchesByDate(date);
    }

    public static DistributionEngine create(LocalDate date) {
        return new DistributionEngine(date);
    }

    public Soldier[][] distribute(List<Soldier> soldiers, List<WatchPoint> watchPoints) {
        long startTime = System.currentTimeMillis();
        int soldierCountForWatch = watchPoints.stream().mapToInt(WatchPoint::getRequiredSoldierCount).sum();
        Soldier[][] result = new Soldier[Settings.getTotalWatchesInDay()][soldierCountForWatch];

        long elapsedTime = 0;
        int trialNum = 0;
        boolean distributed = false;
        boolean perfect = false;
        while (!perfect && (!distributed || (trialNum < 50 && elapsedTime < 30000))) {
            Soldier[][] temp = new Soldier[Settings.getTotalWatchesInDay()][soldierCountForWatch];
            int maxAssignInDayLimit = 8 / Settings.getOneWatchDurationInHours();
            int maxAssigns = 1;
            boolean hasNull = true;
            while (hasNull && maxAssigns <= maxAssignInDayLimit) {
                Random r = new Random();
                Soldier[][] distribution = initDistributionMatrix(watchPoints);

                final int finalMaxAssigns = maxAssigns;
                Arrays.stream(getOrderedWatchValues(soldiers)).forEach(hour -> {
                    int i = hour + Settings.getMinWatchesBetweenTwoWatches();
                    for (int j = 0; j < soldierCountForWatch; j++) {
                        Map<Soldier, Integer> map
                                = getSoldierTicketMapForIndex(distribution,
                                new Index(i, j), soldiers, soldierCountForWatch, finalMaxAssigns);
                        List<Soldier> ticketList = new ArrayList<>();
                        map.entrySet().stream().forEach(
                                e -> IntStream.range(0, e.getValue())
                                        .forEach(value -> ticketList.add(e.getKey()))
                        );
                        Soldier pikachu = ticketList.isEmpty() ? null : ticketList.get(r.nextInt(ticketList.size()));
                        distribution[i][j] = pikachu;
                        temp[i - Settings.getMinWatchesBetweenTwoWatches()][j] = pikachu;
                    }

                });
                hasNull = Arrays.stream(temp).flatMap(Arrays::stream).anyMatch(s -> s == null);
                maxAssigns++;
            }
            trialNum++;
            elapsedTime = System.currentTimeMillis() - startTime;

            long currentNullCount = Arrays.stream(result).flatMap(Arrays::stream).filter(s -> s == null).count();
            long newNullCount = Arrays.stream(temp).flatMap(Arrays::stream).filter(s -> s == null).count();

            logger.debug("Trial: " + trialNum + " - Elapsed: " + elapsedTime + " Empty: " + newNullCount);
            if (newNullCount < currentNullCount) {
                result = temp;
                distributed = true;
            }
            perfect = newNullCount == 0;
        }
        return shuffleRows(result);
    }

    private int[] getOrderedWatchValues(Collection<Soldier> soldiers) {
        return watchValues.stream().sorted((o1, o2) -> {
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

    private Soldier[][] initDistributionMatrix(List<WatchPoint> watchPoints) {
        int soldierCount = watchPoints.stream().mapToInt(WatchPoint::getRequiredSoldierCount).sum();
        Soldier[][] distribution = new Soldier[Settings.getTotalWatchesInDay() +
                (2 * Settings.getMinWatchesBetweenTwoWatches())][soldierCount];
        int lineNum = 0;
        for (int i = Settings.getTotalWatchesInDay() - Settings.getMinWatchesBetweenTwoWatches();
             i < Settings.getTotalWatchesInDay(); i++) {
            final int finalI = i;
            List<Watch> watches = previousDaysWatches.stream()
                    .filter(w -> w.getHour() == finalI).collect(toList());
            List<Soldier> watchSoldiers = watches.parallelStream().map(Watch::getSoldier).collect(toList());
            distribution[lineNum] = watchSoldiers.toArray(new Soldier[watchSoldiers.size()]);
            lineNum++;
        }
        return distribution;
    }

    private Map<Soldier, Integer> getSoldierTicketMapForIndex(Soldier[][] distribution,
                                                              Index index, List<Soldier> soldiers,
                                                              int soldierCountForWatch, int maxAssigns) {
        if (index.getI() < Settings.getMinWatchesBetweenTwoWatches())
            throw new IllegalArgumentException("\"i\" should be equal or higher than " + Settings
                    .getMinWatchesBetweenTwoWatches());
        Soldier[][] temp = cloneArray(distribution);
        temp[index.getI()][index.getJ()] = null;

        Set<Soldier> unavailables
                = getUnavailablesForIndex(distribution, index, soldiers, soldierCountForWatch, maxAssigns);

        Set<Soldier> availables = soldiers.stream().filter(s -> !unavailables.contains(s)).collect(toSet());
        int dayNum = date.getDayOfWeek().getValue() - 1;
        double pointScale = availables.stream().mapToDouble(Soldier::getPoints).sum();
        Map<Soldier, Integer> availablesTicketMap = availables.stream().collect(toMap(s -> s, s -> {
                    return calculateTicketCountForSoldier(s, dayNum, pointScale);

                }
        ));

        return Stream.of(unavailables.stream().collect(toMap(s -> s, s -> 0)), availablesTicketMap)
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Integer calculateTicketCountForSoldier(Soldier soldier, int dayNum, double pointScale) {
        // tickets from availability times (inverse proportion)
        long availabilityCountInDay
                = soldier.getAvailabilities().stream()
                .filter(availability -> availability.getDayNum() == dayNum).count();
        int ticketCountFromAvailability = (int) (AVAILABILITY_EFFECT - (availabilityCountInDay - 1) *
                (AVAILABILITY_EFFECT / (Settings.getTotalWatchesInDay() - 1)));

        // tickets from points (inverse proportion)
        double rate = pointScale != 0 ? soldier.getPoints() / pointScale : 0;
        int ticketCountFromPoints = (int) (POINTS_EFFECT - (POINTS_EFFECT * rate));
        return ticketCountFromAvailability + ticketCountFromPoints;
    }

    private Set<Soldier> getUnavailablesForIndex(Soldier[][] distribution,
                                                 Index index, List<Soldier> soldiers,
                                                 int soldierCountForWatch, int maxAssigns) {
        Set<Soldier> availables = new HashSet<>(soldiers);
        availables.removeAll(soldiers.stream().filter(Soldier::isSergeant).collect(toSet()));
        availables.removeAll(soldiers.stream().filter(s -> !s.isAvailable()).collect(toSet()));

        Set<Soldier> unusedFixedAtThatHour = availables.stream().filter(Soldier::isFixedWatch)
                .filter(s -> s != null)
                .filter(s -> s.getAvailabilities()
                        .contains(new Availability(s, date.getDayOfWeek().getValue() - 1,
                                index.getI() - Settings.getMinWatchesBetweenTwoWatches())))
                .filter(s -> !Arrays.asList(distribution[index.getI()]).contains(s))
                .collect(toSet());

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
                .filter(s -> Collections.frequency(soldiersFlatMap, s) >= maxAssigns).collect(toSet()));

        IntStream.rangeClosed(
                index.getI() - Settings.getMinWatchesBetweenTwoWatches(),
                index.getI() + Settings.getMinWatchesBetweenTwoWatches()
        ).forEach(num -> availables.removeAll(Arrays.asList(distribution[num])));

        availables.removeAll(availables.stream().filter(
                s -> !s.getAvailabilities().contains(new Availability(s, date.getDayOfWeek().getValue() - 1,
                        index.getI() - Settings.getMinWatchesBetweenTwoWatches()))).collect(toSet()));

        // todays finished soldiers
        availables.removeAll(soldiers.stream()
                .filter(s -> s != null)
                .filter(s -> Collections.frequency(soldiersFlatMap, s) >= s.getMaxWatchesPerDay()
                ).collect(toList()));

        return soldiers.stream().filter(s -> s != null && !availables.contains(s)).collect(toSet());
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
