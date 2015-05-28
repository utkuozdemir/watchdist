package tsk.jgnk.watchdist.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import javafx.util.StringConverter;
import tsk.jgnk.watchdist.domain.Availability;
import tsk.jgnk.watchdist.domain.Soldier;
import tsk.jgnk.watchdist.domain.WatchPoint;
import tsk.jgnk.watchdist.fx.SoldierFX;
import tsk.jgnk.watchdist.fx.WatchPointFX;

import java.util.Collection;
import java.util.Comparator;

public class Constants {
    public static final String DB_NAME = "nobet_veritabani.db";
    public static final String TEMPLATE_NAME = "excel_sablonu.xls";
    public static final String TEMPLATE_DAY_NAME = "%GÜN%";

    public static final String DATE_FORMAT = "dd.MM.yyyy";

    public static final Comparator<WatchPoint> WATCH_POINT_COMPARATOR = (o1, o2) -> {
        if (o1 == null && o2 == null) return 0;
        if (o1 == null) return 1;
        if (o2 == null) return -1;
        return o1.getId().compareTo(o2.getId());
    };

    public static final StringConverter<String> STRING_STRING_CONVERTER = new StringConverter<String>() {
        @Override
        public String toString(String s) {
            return s;
        }

        @Override
        public String fromString(String s) {
            return s;
        }
    };

    public static final Function<Soldier, SoldierFX> SOLDIER_TO_FX = s -> {
        if (s == null) return null;
        return new SoldierFX(s.getId(), s.getFullName(), s.getDuty(), s.isAvailable(),
                s.getPoints(), s.isActive(), s.getAvailabilities());
    };

    public static final Function<SoldierFX, Soldier> FX_TO_SOLDIER = sfx -> {
        if (sfx == null) return null;

        Collection<Availability> availabilities
                = Collections2.transform(sfx.availabilitiesProperties(),
                input -> {
                    if (input == null) return null;
                    Availability value = input.getValue();
                    return new Availability(value.getId(), value.getSoldier(), value.getDayNum(), value.getHour());
                });

        return new Soldier(sfx.idProperty().get(), sfx.fullNameProperty().get(), sfx.dutyProperty().get(),
                sfx.availableProperty().get(), sfx.pointsProperty().get(), sfx.activeProperty().get(),
                availabilities
        );
    };

    public static final Function<WatchPointFX, WatchPoint> FX_TO_WATCH_POINT
            = input -> {
        if (input == null) return null;
        return new WatchPoint(input.idProperty().get(), input.nameProperty().get(),
                input.requiredSoldierCountProperty().get(), input.activeProperty().get());
    };

    public static final Function<WatchPoint, WatchPointFX> WATCH_POINT_TO_FX
            = input -> {
        if (input == null) return null;
        return new WatchPointFX(input.getId(), input.getName(), input.getRequiredSoldierCount(), input.isActive());
    };

}
