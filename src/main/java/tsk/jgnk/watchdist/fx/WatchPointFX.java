package tsk.jgnk.watchdist.fx;

import com.google.common.base.Strings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import tsk.jgnk.watchdist.util.Constants;
import tsk.jgnk.watchdist.util.DbManager;

@SuppressWarnings("unused")
public class WatchPointFX {
    private SimpleIntegerProperty id;
    private SimpleStringProperty name;
    private SimpleIntegerProperty requiredSoldierCount;
    private SimpleBooleanProperty active;

    public WatchPointFX(int id, String name, int requiredSoldierCount, boolean active) {
        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.requiredSoldierCount = new SimpleIntegerProperty(requiredSoldierCount);
        this.active = new SimpleBooleanProperty(active);

        addListeners();
    }

    private void addListeners() {
        this.name.addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observableValue, String s, String t1) {
                if (Strings.isNullOrEmpty(t1)) {
                    WatchPointFX.this.name.set(s);
                }
                DbManager.updateWatchPoint(Constants.FX_TO_WATCH_POINT.apply(WatchPointFX.this));
            }
        });

        this.requiredSoldierCount.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                int value = t1.intValue();
                if (value < 0) value = number.intValue();
                else if (value > 10) value = 10;
                WatchPointFX.this.requiredSoldierCount.set(value);
                DbManager.updateWatchPoint(Constants.FX_TO_WATCH_POINT.apply(WatchPointFX.this));
            }
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
