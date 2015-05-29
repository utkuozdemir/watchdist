package tsk.jgnk.watchdist.fx;

import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import javafx.beans.property.*;
import tsk.jgnk.watchdist.domain.Availability;
import tsk.jgnk.watchdist.util.Constants;
import tsk.jgnk.watchdist.util.DbManager;

import java.util.ArrayList;
import java.util.Collection;

@SuppressWarnings("unused")
public class SoldierFX {
    private SimpleIntegerProperty id;
    private SimpleStringProperty fullName;
    private SimpleStringProperty duty;
    private SimpleBooleanProperty available;
    private SimpleDoubleProperty points;
    private SimpleBooleanProperty active;
    private SimpleBooleanProperty sergeant;

    private Collection<SimpleObjectProperty<Availability>> availabilities;
    private SimpleBooleanProperty[][] availabilitiesBooleans;

    public SoldierFX(int id, String fullName, String duty,
                     boolean available, double points, boolean active, boolean sergeant,
                     Collection<Availability> availabilities) {


        this.id = new SimpleIntegerProperty(id);
        this.fullName = new SimpleStringProperty(fullName);
        this.duty = new SimpleStringProperty(duty);
        this.available = new SimpleBooleanProperty(available);
        this.points = new SimpleDoubleProperty(points);
        this.active = new SimpleBooleanProperty(active);
        this.sergeant = new SimpleBooleanProperty(sergeant);

        this.availabilities = Collections2.transform(availabilities, SimpleObjectProperty::new);

        refreshAvailabilitiesBooleansFromAvailabilities();

        addListeners();
    }

    private void refreshAvailabilitiesBooleansFromAvailabilities() {
        availabilitiesBooleans = new SimpleBooleanProperty[7][12];
        for (SimpleObjectProperty<Availability> a : availabilities) {
            Availability availability = a.get();
            availabilitiesBooleans[availability.getDayNum()][availability.getHour()] = new SimpleBooleanProperty(true);
        }

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 12; j++) {
                if (availabilitiesBooleans[i][j] == null)
                    availabilitiesBooleans[i][j] = new SimpleBooleanProperty(false);
            }
        }
    }

    private void refreshAvailabilitiesFromAvailabilitiesBooleans() {
        this.availabilities = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 12; j++) {
                SimpleBooleanProperty abProperty = availabilitiesBooleans[i][j];
                if (abProperty.get()) {
                    Availability availability = new Availability(Constants.FX_TO_SOLDIER.apply(this), i, j);
                    this.availabilities.add(new SimpleObjectProperty<>(availability));
                }
            }
        }
    }

    private void addListeners() {
        this.fullName.addListener((observableValue, s, t1) -> {
            DbManager.updateSoldier(Constants.FX_TO_SOLDIER.apply(SoldierFX.this));
        });

        this.duty.addListener((observableValue, s, t1) -> {
            DbManager.updateSoldier(Constants.FX_TO_SOLDIER.apply(SoldierFX.this));
        });

        this.available.addListener((observableValue, aBoolean, t1) -> {
            if (this.sergeantProperty().get()) this.availableProperty().set(false);
            DbManager.updateSoldier(Constants.FX_TO_SOLDIER.apply(SoldierFX.this));
        });

        this.sergeant.addListener((observable, oldValue, newValue) -> {
            if (newValue) SoldierFX.this.available.set(false);
            DbManager.updateSoldier(Constants.FX_TO_SOLDIER.apply(SoldierFX.this));
        });

        for (SimpleObjectProperty<Availability> availability : availabilities) {
            availability.addListener((observableValue, availability1, t1) -> {
                DbManager.updateSoldier(Constants.FX_TO_SOLDIER.apply(SoldierFX.this));
            });
        }

        this.points.addListener((observableValue, number, t1) -> {
            if (t1.doubleValue() < 0) {
                SoldierFX.this.points.set(number.doubleValue());
            }
            DbManager.updateSoldier(Constants.FX_TO_SOLDIER.apply(SoldierFX.this));
        });

        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 12; j++) {
                SimpleBooleanProperty property = availabilitiesBooleans[i][j];
                property.addListener((observableValue, aBoolean, t1) -> {
                    refreshAvailabilitiesFromAvailabilitiesBooleans();
                    DbManager.updateSoldier(Constants.FX_TO_SOLDIER.apply(SoldierFX.this));
                });
            }
        }
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public SimpleStringProperty fullNameProperty() {
        return fullName;
    }

    public SimpleStringProperty dutyProperty() {
        return duty;
    }

    public SimpleBooleanProperty availableProperty() {
        return available;
    }

    public SimpleDoubleProperty pointsProperty() {
        return points;
    }

    public SimpleBooleanProperty activeProperty() {
        return active;
    }

    public SimpleBooleanProperty sergeantProperty() {
        return sergeant;
    }

    public Collection<SimpleObjectProperty<Availability>> availabilitiesProperties() {
        return availabilities;
    }

    public SimpleBooleanProperty[][] availabilitiesBooleansProperties() {
        return availabilitiesBooleans;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoldierFX soldierFX = (SoldierFX) o;
        return Objects.equal(id, soldierFX.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return fullName.get();
    }
}
