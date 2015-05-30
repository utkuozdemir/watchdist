package tsk.jgnk.watchdist.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.joda.time.LocalDate;
import tsk.jgnk.watchdist.domain.Availability;
import tsk.jgnk.watchdist.domain.Soldier;
import tsk.jgnk.watchdist.domain.Watch;

import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class DistributionEngine {
	public static Soldier[][] createDistribution(LocalDate date) {
		checkNotNull(date);

        int dayNum = date.getDayOfWeek() - 1;

        List<Soldier> soldiers = DbManager.findAllActiveSoldiersOrderedByPointsAsc();

        int pointCount = WatchPointUtil.getTotalWatchPointSoldierCount(DbManager.findAllActiveWatchPoints());

		Soldier[][] distribution = new Soldier[Constants.TOTAL_WATCHES_IN_DAY][pointCount];

        boolean possible = true;
        int iteration = 0;

        while (possible) {
			for (int i = 0; i < Constants.TOTAL_WATCHES_IN_DAY; i++) {
				Set<Soldier> previousSoldiers = new HashSet<>();
				if (i > 0) {
                    previousSoldiers.addAll(Arrays.asList(distribution[i - 1]));
                } else if (i == 0) {
                    List<Watch> yesterdaysLastWatches = DbManager.findWatchesByDateAndHour(date.minusDays(1), 11);
                    List<Soldier> yesterdaysLastWatchSoldiers
							= Lists.transform(yesterdaysLastWatches, input -> input != null ? input.getSoldier() : null);

                    previousSoldiers.addAll(yesterdaysLastWatchSoldiers);
                }

                for (int j = 0; j < pointCount; j++) {
                    if (distribution[i][j] == null) {
                        Set<Soldier> thisHoursSoldiers = new HashSet<>();
                        for (int k = 0; k < pointCount; k++) {
                            if (distribution[i][k] != null) thisHoursSoldiers.add(distribution[i][k]);
                        }

                        List<Soldier> allCurrentlyAssignedSoldiers = new ArrayList<>();
                        for (Soldier[] ss : distribution) {
                            Collections.addAll(allCurrentlyAssignedSoldiers, ss);
                        }
                        allCurrentlyAssignedSoldiers.remove(null);

                        List<Soldier> soldiersAvailableForCell = new ArrayList<>();
                        // remove soldiers who cannot have watch
                        for (Soldier soldier : soldiers) {
                            boolean available = soldier.isAvailable();

                            boolean availableAtHour = false;
                            for (Availability availability : soldier.getAvailabilities()) {
                                if (availability.getDayNum() == dayNum && availability.getHour() == i)
                                    availableAtHour = true;
                            }

                            boolean notAssignedToSameHour = !thisHoursSoldiers.contains(soldier);
                            boolean didNotHaveWatchAtPreviousHour = !previousSoldiers.contains(soldier);

							List<Soldier> assignedSoldiersList = new ArrayList<>();
							for (Soldier[] ss : distribution) {
								Collections.addAll(assignedSoldiersList, ss);
							}

							boolean didNotReachMaxWatchesPerDay = Collections.frequency(assignedSoldiersList, soldier) <
									soldier.getMaxWatchesPerDay();

                            int assignedNtimesThisDay = Collections.frequency(allCurrentlyAssignedSoldiers, soldier);
                            boolean notAssignedMoreThanIterationNumber = (iteration + 1) > assignedNtimesThisDay;

							if (available && availableAtHour && notAssignedToSameHour && didNotReachMaxWatchesPerDay &&
									didNotHaveWatchAtPreviousHour && notAssignedMoreThanIterationNumber) {
								soldiersAvailableForCell.add(soldier);
                            }
                        }

                        List<Soldier> soldiersWithOnlyGivenHourAvailable
                                = getSoldiersWithOnlyGivenHourAvailable(soldiersAvailableForCell, dayNum, i);

                        if (soldiersAvailableForCell.isEmpty()) {
                            distribution[i][j] = null;
                        } else {
                            if (soldiersWithOnlyGivenHourAvailable.isEmpty()) {
                                distribution[i][j] = soldiersAvailableForCell.get(0);
                            } else {
                                distribution[i][j] = soldiersWithOnlyGivenHourAvailable.get(0);
                            }
                        }
                    }
                }
            }
            iteration++;

            // temporary
            if (iteration > 50) possible = false;
        }
        return shuffleRows(distribution);
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


    public static List<Soldier> getSoldiersWithOnlyGivenHourAvailable(
            Collection<Soldier> soldiers, final int dayNum, final int time) {

		List<Soldier> filtered = soldiers.stream().filter(input -> {
			Collection<Availability> availabilities = input.getAvailabilities();
			for (Availability availability : availabilities) {
				if (availability.getDayNum() == dayNum && availability.getHour() != time) {
					return false;
				}
			}
			return true;
		}).collect(Collectors.toList());
		return ImmutableList.copyOf(filtered);
	}
}
