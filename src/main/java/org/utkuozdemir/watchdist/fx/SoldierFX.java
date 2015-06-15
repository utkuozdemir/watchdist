package org.utkuozdemir.watchdist.fx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.Availability;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.util.DbManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.IntStream;


@SuppressWarnings("unused")
public class SoldierFX extends FxWrapper<Soldier> {
	private final SimpleIntegerProperty id;
	private final SimpleStringProperty fullName;
	private final SimpleStringProperty duty;
	private final SimpleBooleanProperty available;
	private final SimpleDoubleProperty points;
	private final SimpleBooleanProperty active;
	private final SimpleBooleanProperty sergeant;
	private final SimpleIntegerProperty maxWatchesPerDay;
	private final SimpleIntegerProperty order;
	private final SimpleBooleanProperty[][] availabilities;
	private final SimpleBooleanProperty fixedWatch;

	public SoldierFX(Soldier soldier) {
		super(soldier);
		this.id = new SimpleIntegerProperty(soldier.getId());
		this.fullName = new SimpleStringProperty(soldier.getFullName());
		this.duty = new SimpleStringProperty(soldier.getDuty());
		this.available = new SimpleBooleanProperty(soldier.isAvailable());
		this.points = new SimpleDoubleProperty(soldier.getPoints());
		this.active = new SimpleBooleanProperty(soldier.isActive());
		this.sergeant = new SimpleBooleanProperty(soldier.isSergeant());
		this.maxWatchesPerDay = new SimpleIntegerProperty(soldier.getMaxWatchesPerDay());
		this.order = new SimpleIntegerProperty(soldier.getOrder());
		this.fixedWatch = new SimpleBooleanProperty(soldier.isFixedWatch());

		Collection<Availability> availabilities = soldier.getAvailabilities();

		this.availabilities = new SimpleBooleanProperty[7][Settings.getTotalWatchesInDay()];
		IntStream.range(0, this.availabilities.length)
				.forEach(i -> IntStream.range(0, this.availabilities[i].length)
						.forEach(j -> this.availabilities[i][j] = new SimpleBooleanProperty(
								availabilities.stream().filter(a -> a.getDayNum() == i && a.getHour() == j)
										.findAny().isPresent()
						)));
		addListeners();
	}

	private void addListeners() {
		this.fullName.addListener((observableValue, oldValue, newValue) -> {
			entityUpdated();
		});

		this.duty.addListener((observableValue, oldValue, newValue) -> {
			entityUpdated();
		});

		this.available.addListener((observableValue, aBoolean, newValue) -> {
			if (this.sergeantProperty().get()) this.availableProperty().set(false);
			entityUpdated();
		});

		this.sergeant.addListener((observable, oldValue, newValue) -> {
			if (newValue) SoldierFX.this.available.set(false);
			entityUpdated();
		});

		this.points.addListener((observableValue, number, newValue) -> {
			if (newValue.doubleValue() < 0) {
				SoldierFX.this.points.set(number.doubleValue());
			}
			entityUpdated();
		});

		this.maxWatchesPerDay.addListener((observable, oldValue, newValue) -> {
			if (newValue.intValue() < 1 || newValue.intValue() > Settings.getTotalWatchesInDay()) {
				SoldierFX.this.maxWatchesPerDay.set(oldValue.intValue());
			}
			entityUpdated();
		});

		this.order.addListener((observable, oldValue, newValue) -> {
			entityUpdated();
		});

		this.fixedWatch.addListener((observable1, oldValue1, newValue1) -> entityUpdated());

		Arrays.stream(availabilities).flatMap(Arrays::stream)
				.forEach(ab -> ab.addListener((observable, oldValue, newValue) -> entityUpdated()));
	}

	public void commit() {
		entity.setId(id.get());
		entity.setFullName(fullName.get());
		entity.setDuty(duty.get());
		entity.setActive(active.get());
		entity.setSergeant(sergeant.get());
		entity.setAvailable(available.get());
		entity.setMaxWatchesPerDay(maxWatchesPerDay.get());
		entity.setOrder(order.get());
		entity.setPoints(points.get());
		entity.setFixedWatch(fixedWatch.get());

		Set<Availability> availabilities = new HashSet<>();
		IntStream.range(0, this.availabilities.length)
				.forEach(i -> IntStream.range(0, this.availabilities[i].length)
						.forEach(j -> {
							if (this.availabilities[i][j].get())
								availabilities.add(new Availability(entity, i, j));
						}));

		entity.setAvailabilities(availabilities);
		DbManager.saveSoldier(entity);
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

	public SimpleBooleanProperty fixedWatchProperty() {
		return fixedWatch;
	}

	public SimpleBooleanProperty[][] availabilitiesProperties() {
		return availabilities;
	}

	@Override
	public String toString() {
		return fullName.get();
	}
}
