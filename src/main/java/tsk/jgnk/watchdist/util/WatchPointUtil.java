package tsk.jgnk.watchdist.util;

import tsk.jgnk.watchdist.domain.WatchPoint;

import java.util.Collection;

public class WatchPointUtil {
    public static int getTotalWatchPointSoldierCount(Collection<WatchPoint> watchPoints) {
        int value = 0;
        for (WatchPoint point : watchPoints) {
            value += point.getRequiredSoldierCount();
        }
        return value;
    }
}
