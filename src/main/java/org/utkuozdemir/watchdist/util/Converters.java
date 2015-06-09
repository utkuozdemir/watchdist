package org.utkuozdemir.watchdist.util;

import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.domain.Availability;
import org.utkuozdemir.watchdist.domain.Soldier;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.fx.SoldierFX;
import org.utkuozdemir.watchdist.fx.WatchPointFX;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

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
	public static final Function<Soldier, SoldierFX> SOLDIER_TO_FX = s -> {
		if (s == null) return null;
		return new SoldierFX(s.getId(), s.getFullName(), s.getDuty(), s.isAvailable(),
				s.getPoints(), s.isActive(), s.isSergeant(), s.getMaxWatchesPerDay(), s.getAvailabilities());
	};
	public static final Function<SoldierFX, Soldier> FX_TO_SOLDIER = sfx -> {
		if (sfx == null) return null;

		Collection<Availability> availabilities
				= sfx.availabilitiesProperties().stream().map(input -> {
			if (input == null) return null;
			Availability value = input.getValue();
			return new Availability(value.getId(), value.getSoldier(), value.getDayNum(), value.getHour());
		}).collect(Collectors.toList());

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
