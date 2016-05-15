package org.utkuozdemir.watchdist.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.time.format.DateTimeFormatter;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "watch")
public class Watch {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "soldier")
    private Soldier soldier;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "watch_point")
    private WatchPoint watchPoint;

    @DatabaseField(columnName = "watch_point_slot")
    private int watchPointSlot;

    @DatabaseField(columnName = "date")
    private String date;

    @DatabaseField(columnName = "hour")
    private int hour;

    @DatabaseField(columnName = "points_won")
    private double pointsWon;

    Watch() {
    }

    public Watch(Soldier soldier, WatchPoint watchPoint, int watchPointSlot, String date, int hour, double pointsWon) {
        validateDate(date);
        this.soldier = soldier;
        this.watchPoint = watchPoint;
        this.watchPointSlot = watchPointSlot;
        this.date = date;
        this.hour = hour;
        this.pointsWon = pointsWon;
    }

    private void validateDate(String date) {
        ISO_LOCAL_DATE.parse(date);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Soldier getSoldier() {
        return soldier;
    }

    public void setSoldier(Soldier soldier) {
        this.soldier = soldier;
    }

    public WatchPoint getWatchPoint() {
        return watchPoint;
    }

    public void setWatchPoint(WatchPoint watchPoint) {
        this.watchPoint = watchPoint;
    }

    public int getWatchPointSlot() {
        return watchPointSlot;
    }

    public void setWatchPointSlot(int watchPointSlot) {
        this.watchPointSlot = watchPointSlot;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        validateDate(date);
        this.date = date;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public double getPointsWon() {
        return pointsWon;
    }

    public void setPointsWon(double pointsWon) {
        this.pointsWon = pointsWon;
    }
}
