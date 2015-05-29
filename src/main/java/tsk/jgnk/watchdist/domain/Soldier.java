package tsk.jgnk.watchdist.domain;


import com.google.common.base.Objects;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "soldier")
public class Soldier {
    @DatabaseField(generatedId = true)
    private Integer id;

    @DatabaseField(columnName = "full_name")
    private String fullName;

    @DatabaseField(columnName = "duty")
    private String duty;

    @DatabaseField(columnName = "available")
    private boolean available = true;

    @DatabaseField(columnName = "points")
    private double points = 0.0;

    @DatabaseField(columnName = "active")
    private boolean active = true;

    @DatabaseField(columnName = "sergeant")
    private boolean sergeant;


    @ForeignCollectionField(eager = true)
    private Collection<Availability> availabilities;

    Soldier() {
    }

    public Soldier(String fullName, String duty, boolean available, boolean sergeant) {
        this.fullName = fullName;
        this.duty = duty;
        this.available = available;
        this.sergeant = sergeant;

        this.availabilities = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 12; j++) {
                Availability availability = new Availability(this, i, j);
                this.availabilities.add(availability);
            }
        }
    }

    public Soldier(Integer id, String fullName, String duty, boolean available,
                   double points, boolean active, boolean sergeant, Collection<Availability> availabilities) {
        this.id = id;
        this.fullName = fullName;
        this.duty = duty;
        this.available = available;
        this.points = points;
        this.active = active;
        this.sergeant = sergeant;
        this.availabilities = availabilities;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDuty() {
        return duty;
    }

    public void setDuty(String duty) {
        this.duty = duty;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public double getPoints() {
        return points;
    }

    public void setPoints(double points) {
        this.points = points;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isSergeant() {
        return sergeant;
    }

    public void setSergeant(boolean sergeant) {
        this.sergeant = sergeant;
    }

    public Collection<Availability> getAvailabilities() {
        return availabilities;
    }

    @Override
    public String toString() {
        return fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Soldier soldier = (Soldier) o;
        return Objects.equal(id, soldier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
