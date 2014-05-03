package org.cmdbuild.common;

/**
 * An {@link Holder} that can be used for creating singletons. It's implemented
 * using double-checked locking.
 */
public abstract class SingletonHolder<T> implements Holder<T> {

	private volatile T holded;

	@Override
	public final T get() {
		T holded = this.holded;
		if (holded == null) {
			synchronized (this) {
				holded = this.holded;
				if (holded == null) {
					this.holded = holded = doGet();
				}
			}
		}
		return holded;
	}

	protected abstract T doGet();

}
