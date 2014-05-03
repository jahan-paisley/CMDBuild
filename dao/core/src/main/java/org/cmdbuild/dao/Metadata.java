package org.cmdbuild.dao;

import java.util.HashMap;
import java.util.Map;

public class Metadata {

	private final Map<String, String> metaMap;

	protected Metadata() {
		metaMap = new HashMap<String, String>();
	}

	public String get(final String key) {
		return metaMap.get(key);
	}

	public void put(final String key, final String value) {
		metaMap.put(key, value);
	}

	@Override
	public String toString() {
		return metaMap.toString();
	}

}
