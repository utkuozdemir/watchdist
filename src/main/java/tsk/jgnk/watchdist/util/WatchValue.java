package tsk.jgnk.watchdist.util;

import com.google.common.base.Preconditions;

import java.util.Arrays;

public class WatchValue {
    public static double of(int watchHour) {
        Preconditions.checkArgument(watchHour >= 0 && watchHour <= 11);
        return Arrays.asList(11, 0, 1, 2).contains(watchHour) ? 1.5 : 1.0;
    }
}