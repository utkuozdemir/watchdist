package org.utkuozdemir.watchdist.fx;

import javafx.beans.property.*;
import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.Availability;
import org.utkuozdemir.watchdist.util.Converters;
import org.utkuozdemir.watchdist.util.DbManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;


@SuppressWarnings("unused")
public class SoldierFX {
	private final SimpleIntegerProperty id;
	private final SimpleStringProperty fullName;
	private final SimpleStringProperty duty;
	private final SimpleBooleanProperty available;
	private final SimpleDoubleProperty points;
	private final SimpleBooleanProperty active;
	private final SimpleBooleanProperty sergeant;
	private final SimpleIntegerProperty maxWatchesPerDay;
	private final SimpleIntegerProperty order;

	private Collection<SimpleObjectProperty<Availability>> availabilities;
	private SimpleBooleanProperty[][] availabilitiesBooleans;

	public SoldierFX(int id, String fullName, String duty, boolean available, double points,
					 boolean active, boolean sergeant, int maxWatchesPerDay, int order,
					 Collection<Availability> availabilities) {


		this.id = new SimpleIntegerProperty(id);
		this.fullName = new SimpleStringProperty(fullName);
		this.duty = new SimpleStringProperty(duty);
		this.available = new SimpleBooleanProperty(available);
		this.points = new SimpleDoubleProperty(points);
		this.active = new SimpleBooleanProperty(active);
		this.sergeant = new SimpleBooleanProperty(sergeant);
		this.maxWatchesPerDay = new SimpleIntegerProperty(maxWatchesPerDay);
		this.order = new SimpleIntegerProperty(order);

		this.availabilities = availabilities.stream()
				.map((Function<Availability, SimpleObjectProperty<Availability>>) SimpleObjectProperty::new)
				.collect(Collectors.toList());
		refreshAvailabilitiesBooleansFromAvailabilities();

		addListeners();
	}

	private void refreshAvailabilitiesBooleansFromAvailabilities() {
		availabilitiesBooleans = new SimpleBooleanProperty[7][Settings.getTotalWatchesInDay()];
		for (SimpleObjectProperty<Availability> a : availabilities) {
			Availability availability = a.get();
			availabilitiesBooleans[availability.getDayNum()][availability.getHour()] = new SimpleBooleanProperty(true);
		}

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < Settings.getTotalWatchesInDay(); j++) {
				if (availabilitiesBooleans[i][j] == null)
					availabilitiesBooleans[i][j] = new SimpleBooleanProperty(false);
			}
		}
	}

	private void refreshAvailabilitiesFromAvailabilitiesBooleans() {
		this.availabilities = new ArrayList<>();
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < Settings.getTotalWatchesInDay(); j++) {
				SimpleBooleanProperty abProperty = availabilitiesBooleans[i][j];
				if (abProperty.get()) {
					Availability availability = new Availability(Converters.FX_TO_SOLDIER.apply(this), i, j);
					this.availabilities.add(new SimpleObjectProperty<>(availability));
				}
			}
		}
	}

	private void addListeners() {
		this.fullName.addListener((observableValue, s, t1) -> {
			DbManager.saveSoldier(Converters.FX_TO_SOLDIER.apply(SoldierFX.this));
		});

		this.duty.addListener((observableValue, s, t1) -> {
			DbManager.saveSoldier(Converters.FX_TO_SOLDIER.apply(SoldierFX.this));
		});

		this.available.addListener((observableValue, aBoolean, t1) -> {
			if (this.sergeantProperty().get()) this.availableProperty().set(false);
			DbManager.saveSoldier(Converters.FX_TO_SOLDIER.apply(SoldierFX.this));
		});

		this.sergeant.addListener((observable, oldValue, newValue) -> {
			if (newValue) SoldierFX.this.available.set(false);
			DbManager.saveSoldier(Converters.FX_TO_SOLDIER.apply(SoldierFX.this));
		});

		for (SimpleObjectProperty<Availability> availability : availabilities) {
			availability.addListener((observableValue, availability1, t1) -> {
				DbManager.saveSoldier(Converters.FX_TO_SOLDIER.apply(SoldierFX.this));
			});
		}

		this.points.addListener((observableValue, number, t1) -> {
			if (t1.doubleValue() < 0) {
				SoldierFX.this.points.set(number.doubleValue());
			}
			DbManager.saveSoldier(Converters.FX_TO_SOLDIER.apply(SoldierFX.this));
		});

		this.maxWatchesPerDay.addListener((observable, oldValue, newValue) -> {
			if (newValue.intValue() < 1 || newValue.intValue() > Settings.getTotalWatchesInDay()) {
				SoldierFX.this.maxWatchesPerDay.set(oldValue.intValue());
			}
			DbManager.saveSoldier(Converters.FX_TO_SOLDIER.apply(SoldierFX.this));
		});

		this.order.addListener((observable, oldValue, newValue) -> {
			DbManager.saveSoldier(Converters.FX_TO_SOLDIER.apply(SoldierFX.this));
		});

		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < Settings.getTotalWatchesInDay(); j++) {
				SimpleBooleanProperty property = availabilitiesBooleans[i][j];
				property.addListener((observableValue, aBoolean, t1) -> {
					refreshAvailabilitiesFromAvailabilitiesBooleans();
					DbManager.saveSoldier(Converters.FX_TO_SOLDIER.apply(SoldierFX.this));
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

	public SimpleIntegerProperty maxWatchesPerDayProperty() {
		return maxWatchesPerDay;
	}

	public SimpleIntegerProperty orderProperty() {
		return order;
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
		return Objects.equals(id, soldierFX.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return fullName.get();
	}
}
