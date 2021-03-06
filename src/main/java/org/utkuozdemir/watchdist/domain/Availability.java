package org.utkuozdemir.watchdist.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "availability")
public class Availability {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "soldier")
    private Soldier soldier;

    @DatabaseField(columnName = "day_num")
    private int dayNum;

    @DatabaseField(columnName = "hour")
    private int hour;

    Availability() {
    }

    public Availability(Soldier soldier, int dayNum, int hour) {
        this.soldier = soldier;
        this.dayNum = dayNum;
        this.hour = hour;
    }

    public Availability(Integer id, Soldier soldier, int dayNum, int hour) {
        this.id = id;
        this.soldier = soldier;
        this.dayNum = dayNum;
        this.hour = hour;
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

    public int getDayNum() {
        return dayNum;
    }

    public void setDayNum(int dayNum) {
        this.dayNum = dayNum;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Availability that = (Availability) o;
        return Objects.equals(dayNum, that.dayNum) &&
                Objects.equals(hour, that.hour) &&
                Objects.equals(soldier, that.soldier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(soldier, dayNum, hour);
    }
}
