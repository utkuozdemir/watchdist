package org.utkuozdemir.watchdist.util;

import org.utkuozdemir.watchdist.domain.WatchPoint;

import java.util.Comparator;

public class Comparators {
	public static final Comparator<WatchPoint> WATCH_POINT_ID_ASC_COMPARATOR = (o1, o2) -> {
		if (o1 == null && o2 == null) return 0;
		if (o1 == null) return 1;
		if (o2 == null) return -1;
		return o1.getId().compareTo(o2.getId());
	};
}
