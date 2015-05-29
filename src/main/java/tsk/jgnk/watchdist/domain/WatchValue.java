package tsk.jgnk.watchdist.domain;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import static com.google.common.base.Preconditions.checkArgument;

@SuppressWarnings("unused")
@DatabaseTable(tableName = "watch_value")
public class WatchValue {
    @DatabaseField(id = true, columnName = "hour")
    private int hour;

    @DatabaseField(columnName = "value")
    private double value;

    WatchValue() {
    }

    public WatchValue(int hour, double value) {
        checkArgument(hour >= 0 && hour <= 11);
        checkArgument(value > 0);
        this.hour = hour;
        this.value = value;
    }

    public int getHour() {
        return hour;
    }

    public void setHour(int hour) {
        checkArgument(hour >= 0 && hour <= 11);
        this.hour = hour;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        checkArgument(value > 0);
        this.value = value;
    }
}
