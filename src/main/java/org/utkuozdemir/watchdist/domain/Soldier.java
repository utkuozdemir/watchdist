package org.utkuozdemir.watchdist.domain;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;
import org.utkuozdemir.watchdist.app.Settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

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

	@DatabaseField(columnName = "max_watches_per_day")
	private int maxWatchesPerDay;

    @ForeignCollectionField(eager = true)
    private Collection<Availability> availabilities;

    Soldier() {
    }

	public Soldier(String fullName, String duty, boolean available, boolean sergeant, int maxWatchesPerDay) {
        if (maxWatchesPerDay < 1 || maxWatchesPerDay > Settings.getTotalWatchesInDay())
            throw new IllegalArgumentException("Invalid value for maxWatchesPerDay!");

        this.fullName = fullName;
        this.duty = duty;
        this.available = available;
        this.sergeant = sergeant;
		this.maxWatchesPerDay = maxWatchesPerDay;

		this.availabilities = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
            for (int j = 0; j < Settings.getTotalWatchesInDay(); j++) {
                Availability availability = new Availability(this, i, j);
				this.availabilities.add(availability);
            }
        }
    }

    public Soldier(Integer id, String fullName, String duty, boolean available,
				   double points, boolean active, boolean sergeant, int maxWatchesPerDay,
				   Collection<Availability> availabilities) {
		this.id = id;
		this.fullName = fullName;
        this.duty = duty;
        this.available = available;
        this.points = points;
        this.active = active;
        this.sergeant = sergeant;
		this.maxWatchesPerDay = maxWatchesPerDay;
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

	public int getMaxWatchesPerDay() {
		return maxWatchesPerDay;
	}

	public void setMaxWatchesPerDay(int maxWatchesPerDay) {
        if (maxWatchesPerDay < 1 || maxWatchesPerDay > Settings.getTotalWatchesInDay())
            throw new IllegalArgumentException("Invalid value for maxWatchesPerDay!");
		this.maxWatchesPerDay = maxWatchesPerDay;
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
        return Objects.equals(id, soldier.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
