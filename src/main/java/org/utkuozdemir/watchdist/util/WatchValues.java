package org.utkuozdemir.watchdist.util;

import org.utkuozdemir.watchdist.Settings;
import org.utkuozdemir.watchdist.domain.WatchValue;

import java.util.Map;
import java.util.stream.Collectors;

public class WatchValues {
    private static Map<Integer, Double> values;


    public static void refresh() {
        values = DbManager.findAllWatchValues().stream()
                .collect(Collectors.toMap(WatchValue::getHour, WatchValue::getValue));
    }

    public static double get(int hour) {
        if (hour < 0 || hour >= Settings.getTotalWatchesInDay())
            throw new IllegalArgumentException("Invalid hour: " + hour);
        if (values == null) refresh();
        return values.get(hour);
    }

    public static Map<Integer, Double> getAllValues() {
        if (values == null) refresh();
        return values;
    }
}
