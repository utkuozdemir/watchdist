package org.utkuozdemir.watchdist.util;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.utkuozdemir.watchdist.domain.Soldier;

@SuppressWarnings({"unused", "unchecked"})
public class DistributionRow {
    private final SimpleStringProperty hours;
    private SimpleObjectProperty<Soldier>[] soldiers;

    public DistributionRow(String hours, Soldier[] soldiers) {
        this.hours = new SimpleStringProperty(hours);

        this.soldiers = new SimpleObjectProperty[soldiers.length];
        for (int i = 0; i < soldiers.length; i++) {
            this.soldiers[i] = new SimpleObjectProperty<>(soldiers[i]);
        }
    }

    public String getHours() {
        return hours.get();
    }

    public void setHours(String hours) {
        this.hours.set(hours);
    }

    public SimpleStringProperty hoursProperty() {
        return hours;
    }

    public Soldier[] getSoldiers() {
        Soldier[] soldiers = new Soldier[this.soldiers.length];
        for (int i = 0; i < this.soldiers.length; i++) {
            soldiers[i] = this.soldiers[i].get();
        }
        return soldiers;
    }

    public void setSoldiers(Soldier[] soldiers) {
        this.soldiers = new SimpleObjectProperty[soldiers.length];
        for (int i = 0; i < soldiers.length; i++) {
            this.soldiers[i] = new SimpleObjectProperty<>(soldiers[i]);
        }
    }

    public SimpleObjectProperty<Soldier>[] soldiersProperties() {
        return this.soldiers;
    }
}
