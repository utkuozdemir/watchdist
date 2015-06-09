package org.utkuozdemir.watchdist.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.utkuozdemir.watchdist.Settings;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "watch_value")
public class WatchValue {
    @DatabaseField(id = true, columnName = "hour")
    private int hour;

    @DatabaseField(columnName = "value")
    private double value;

    WatchValue() {
    }

    public WatchValue(int hour, double value) {
        if (hour < 0 || hour >= Settings.getTotalWatchesInDay()) throw new IllegalArgumentException();
        if (value <= 0) throw new IllegalArgumentException();
        this.hour = hour;
        this.value = value;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        if (hour < 0 || hour >= Settings.getTotalWatchesInDay()) throw new IllegalArgumentException();
        this.hour = hour;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        if (value <= 0) throw new IllegalArgumentException();
        this.value = value;
    }
}
