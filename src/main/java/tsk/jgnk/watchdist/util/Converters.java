package tsk.jgnk.watchdist.util;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tsk.jgnk.watchdist.domain.Availability;
import tsk.jgnk.watchdist.domain.Soldier;
import tsk.jgnk.watchdist.domain.WatchPoint;
import tsk.jgnk.watchdist.domain.WatchValue;
import tsk.jgnk.watchdist.fx.SoldierFX;
import tsk.jgnk.watchdist.fx.WatchPointFX;
import tsk.jgnk.watchdist.fx.WatchValueFX;

import java.util.Collection;

public class Converters {
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
	public static final Function<WatchValue, WatchValueFX> WATCH_VALUE_TO_FX
			= input -> input != null ? new WatchValueFX(input.getHour(), input.getValue()) : null;
	public static final Function<Soldier, SoldierFX> SOLDIER_TO_FX = s -> {
		if (s == null) return null;
		return new SoldierFX(s.getId(), s.getFullName(), s.getDuty(), s.isAvailable(),
				s.getPoints(), s.isActive(), s.isSergeant(), s.getMaxWatchesPerDay(), s.getAvailabilities());
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

		return new Soldier(
				sfx.idProperty().get(), sfx.fullNameProperty().get(), sfx.dutyProperty().get(),
				sfx.availableProperty().get(), sfx.pointsProperty().get(), sfx.activeProperty().get(),
				sfx.sergeantProperty().get(), sfx.maxWatchesPerDayProperty().get(),
				availabilities
		);
	};
	private static final Logger logger = LoggerFactory.getLogger(Converters.class);
	public static final StringConverter<Integer> DOUBLE_INTEGER_CONVERTER = new StringConverter<Integer>() {
		@Override
		public String toString(Integer integer) {
			return String.valueOf(integer);
		}

		@Override
		public Integer fromString(String s) {
			int value = -1;
			try {
				value = Integer.parseInt(s);
			} catch (NumberFormatException e) {
				logger.debug(e.getMessage(), e);
			}
			return value;
		}
	};
	public static final StringConverter<Double> DOUBLE_STRING_CONVERTER = new StringConverter<Double>() {
		@Override
		public String toString(Double aDouble) {
			return String.valueOf(aDouble);
		}

		@Override
		public Double fromString(String s) {
			double value = -1;
			try {
				value = Double.parseDouble(s);
			} catch (NumberFormatException e) {
				logger.debug(e.getMessage(), e);
			}

			return value;
		}
	};
}
