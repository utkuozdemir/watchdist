package org.utkuozdemir.watchdist.fx;

import com.google.common.base.Strings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.utkuozdemir.watchdist.util.Converters;
import org.utkuozdemir.watchdist.util.DbManager;

@SuppressWarnings("unused")
public class WatchPointFX {
    private final SimpleIntegerProperty id;
    private final SimpleStringProperty name;
    private final SimpleIntegerProperty requiredSoldierCount;
    private final SimpleBooleanProperty active;

    public WatchPointFX(int id, String name, int requiredSoldierCount, boolean active) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.requiredSoldierCount = new SimpleIntegerProperty(requiredSoldierCount);
        this.active = new SimpleBooleanProperty(active);

        addListeners();
    }

    private void addListeners() {
        this.name.addListener((observableValue, s, t1) -> {
            if (Strings.isNullOrEmpty(t1)) {
                WatchPointFX.this.name.set(s);
            }
            DbManager.updateWatchPoint(Converters.FX_TO_WATCH_POINT.apply(WatchPointFX.this));
        });

        this.requiredSoldierCount.addListener((observableValue, number, t1) -> {
            int value = t1.intValue();
            if (value < 0) value = number.intValue();
            else if (value > 10) value = 10;
            WatchPointFX.this.requiredSoldierCount.set(value);
            DbManager.updateWatchPoint(Converters.FX_TO_WATCH_POINT.apply(WatchPointFX.this));
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
}
