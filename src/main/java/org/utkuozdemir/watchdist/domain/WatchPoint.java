package org.utkuozdemir.watchdist.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Objects;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "watch_point")
public class WatchPoint {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(columnName = "name")
    private String name;


    @DatabaseField(columnName = "required_soldier_count")
    private int requiredSoldierCount = 1;

    @DatabaseField(columnName = "active")
    private boolean active = true;

    WatchPoint() {
    }

    public WatchPoint(String name, int requiredSoldierCount) {
        this.name = name;
        this.requiredSoldierCount = requiredSoldierCount;
    }

    public WatchPoint(Integer id, String name, int requiredSoldierCount, boolean active) {
        this.id = id;
        this.name = name;
        this.requiredSoldierCount = requiredSoldierCount;
        this.active = active;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequiredSoldierCount() {
        return requiredSoldierCount;
    }

    public void setRequiredSoldierCount(int requiredSoldierCount) {
        this.requiredSoldierCount = requiredSoldierCount;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WatchPoint that = (WatchPoint) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
