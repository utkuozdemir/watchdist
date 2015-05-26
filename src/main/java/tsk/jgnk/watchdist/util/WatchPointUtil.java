package tsk.jgnk.watchdist.util;

import tsk.jgnk.watchdist.domain.WatchPoint;

public class WatchPointUtil {
    public static int getTotalWatchPointSoldierCount() {
        int value = 0;
        for (WatchPoint point : DbManager.findAllActiveWatchPoints()) {
            value += point.getRequiredSoldierCount();
        }
        return value;
    }
}
