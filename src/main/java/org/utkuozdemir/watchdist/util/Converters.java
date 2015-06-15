package org.utkuozdemir.watchdist.util;

import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
