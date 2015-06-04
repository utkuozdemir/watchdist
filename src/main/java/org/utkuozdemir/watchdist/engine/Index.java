package org.utkuozdemir.watchdist.engine;

import com.google.common.base.Objects;

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
		Index ındex = (Index) o;
		return com.google.common.base.Objects.equal(i, ındex.i) &&
				Objects.equal(j, ındex.j);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(i, j);
	}
}