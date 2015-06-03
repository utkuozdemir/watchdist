package org.utkuozdemir.watchdist.util;

import com.google.common.collect.Maps;
import org.utkuozdemir.watchdist.domain.WatchValue;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

public class WatchValues {
    private static Map<Integer, Double> values;


    public static void refresh() {
        values = Maps.transformValues(
                Maps.uniqueIndex(DbManager.findAllWatchValues(), WatchValue::getHour),
                WatchValue::getValue
        );
    }

    public static double get(int hour) {
        checkArgument(hour >= 0 && hour <= 11, "Invalid hour: " + hour);
        if (values == null) refresh();
        return values.get(hour);
    }

    public static Map<Integer, Double> getAllValues() {
        if (values == null) refresh();
        return values;
    }
}
