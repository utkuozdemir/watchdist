package org.utkuozdemir.watchdist.fx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.utkuozdemir.watchdist.util.DbManager;
import org.utkuozdemir.watchdist.util.WatchValues;

@SuppressWarnings("unused")
public class WatchValueFX {
    private final SimpleIntegerProperty hour;
    private final SimpleDoubleProperty value;

    public WatchValueFX(int hour, double value) {
        this.hour = new SimpleIntegerProperty(hour);
        this.value = new SimpleDoubleProperty(value);

        this.value.addListener((observable, oldValue, newValue) -> {
            if (newValue.doubleValue() < 0) WatchValueFX.this.valueProperty().set(oldValue.doubleValue());
            DbManager.updateWatchValue(WatchValueFX.this.hour.get(), WatchValueFX.this.value.get());
            WatchValues.refresh();
        });
    }

    public SimpleIntegerProperty hourProperty() {
        return hour;
    }

    public SimpleDoubleProperty valueProperty() {
        return value;
    }
}
