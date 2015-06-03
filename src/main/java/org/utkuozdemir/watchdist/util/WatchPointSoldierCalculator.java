package org.utkuozdemir.watchdist.util;

import org.utkuozdemir.watchdist.domain.WatchPoint;

import java.util.Collection;

public class WatchPointSoldierCalculator {
    public static int getTotalWatchPointSoldierCount(Collection<WatchPoint> watchPoints) {
        int value = 0;
        for (WatchPoint point : watchPoints) {
            value += point.getRequiredSoldierCount();
        }
        return value;
    }
}
