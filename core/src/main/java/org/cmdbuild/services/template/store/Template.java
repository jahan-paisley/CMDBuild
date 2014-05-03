package org.cmdbuild.services.template.store;

import org.cmdbuild.data.store.Storable;

public class Template implements Storable {

	public static Template of(final String key) {
		return new Template(key, null);
	}

	public static Template of(final String key, final String value) {
		return new Template(key, value);
	}

	private final String key;
	private final String value;

	private Template(final String key, final String value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String getIdentifier() {
		return key;
	}

	public String getKey() {
		return key;
	}

	public String getValue() {
		return value;
	}

}
