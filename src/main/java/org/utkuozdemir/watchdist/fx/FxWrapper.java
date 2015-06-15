package org.utkuozdemir.watchdist.fx;

import java.util.Objects;

public abstract class FxWrapper<T> {
	protected final T entity;
	private boolean implicitCommit = true;

	public FxWrapper(T entity) {
		this.entity = entity;
	}

	public boolean isImplicitCommit() {
		return implicitCommit;
	}

	public void setImplicitCommit(boolean implicitCommit) {
		this.implicitCommit = implicitCommit;
	}

	public T getEntity() {
		return entity;
	}

	protected void entityUpdated() {
		if (implicitCommit) {
			commit();
		}
	}

	protected abstract void commit();

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		FxWrapper<?> fxWrapper = (FxWrapper<?>) o;
		return Objects.equals(entity, fxWrapper.entity);
	}

	@Override
	public int hashCode() {
		return Objects.hash(entity);
	}
}
