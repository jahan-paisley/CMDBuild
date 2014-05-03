package org.cmdbuild.dao.entry;

import java.util.Map;
import java.util.Map.Entry;

public abstract class LazyValueSet implements CMValueSet {

	private volatile Map<String, Object> valueMap = null;

	Map<String, Object> getValueMap() {
		if (valueMap == null) {
			synchronized (this) {
				if (valueMap == null) {
					valueMap = load();
				}
			}
		}
		return valueMap;
	}

	@Override
	public Iterable<Entry<String, Object>> getValues() {
		return getValueMap().entrySet();
	}

	@Override
	public Object get(final String key) {
		return getValueMap().get(key);
	}

	@Override
	public <T> T get(final String key, final Class<? extends T> requiredType) {
		final Object value = get(key);
		return requiredType.cast(value);
	}

	protected abstract Map<String, Object> load();
}
