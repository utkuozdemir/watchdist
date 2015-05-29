package tsk.jgnk.watchdist.fx;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import tsk.jgnk.watchdist.util.DbManager;
import tsk.jgnk.watchdist.util.WatchValues;

@SuppressWarnings("unused")
public class WatchValueFX {
    private SimpleIntegerProperty hour;
    private SimpleDoubleProperty value;

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
