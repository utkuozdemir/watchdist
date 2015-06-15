package org.utkuozdemir.watchdist.fx;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.SaveMode;

@SuppressWarnings("unused")
public class WatchPointFX extends FxWrapper<WatchPoint> {
	private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty requiredSoldierCount;
    private final SimpleBooleanProperty active;
    private final SimpleIntegerProperty order;

	public WatchPointFX(WatchPoint watchPoint) {
		super(watchPoint);
		this.id = new SimpleIntegerProperty(watchPoint.getId());
		this.name = new SimpleStringProperty(watchPoint.getName());
		this.requiredSoldierCount = new SimpleIntegerProperty(watchPoint.getRequiredSoldierCount());
		this.active = new SimpleBooleanProperty(watchPoint.isActive());
		this.order = new SimpleIntegerProperty(watchPoint.getOrder());

        addListeners();
    }

	@Override
	protected void commit() {
		SaveMode saveMode = entity.getRequiredSoldierCount() == requiredSoldierCount.get() ?
				SaveMode.INSERT_OR_UPDATE : SaveMode.REMOVE_OLD_CREATE_NEW;
		entity.setId(id.get());
		entity.setName(name.get());
		entity.setRequiredSoldierCount(requiredSoldierCount.get());
		entity.setActive(active.get());
		entity.setOrder(order.get());

		DbManager.saveWatchPoint(entity, saveMode);

	}

    private void addListeners() {
        this.name.addListener((observableValue, s, t1) -> {
            if (t1 == null || t1.isEmpty()) {
                WatchPointFX.this.name.set(s);
            }
			entityUpdated();
		});

        this.requiredSoldierCount.addListener((observableValue, number, t1) -> {
			entityUpdated();
		});

        this.order.addListener((observable, oldValue, newValue) -> {
			entityUpdated();
		});
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public SimpleIntegerProperty requiredSoldierCountProperty() {
        return requiredSoldierCount;
    }

    public SimpleBooleanProperty activeProperty() {
        return active;
    }

    public SimpleIntegerProperty orderProperty() {
        return order;
    }
}
