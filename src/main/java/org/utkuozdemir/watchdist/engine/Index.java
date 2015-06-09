package org.utkuozdemir.watchdist.engine;

import java.util.Objects;

class Index {
	private final int i;
	private final int j;

	public Index(int i, int j) {
		this.i = i;
		this.j = j;
	}

	public int getI() {
		return i;
	}

	public int getJ() {
		return j;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Index index = (Index) o;
		return Objects.equals(i, index.i) &&
				Objects.equals(j, index.j);
	}

	@Override
	public int hashCode() {
		return Objects.hash(i, j);
	}
}