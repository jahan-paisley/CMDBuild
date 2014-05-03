package org.cmdbuild.common.collect;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Factory {

	public static Map<String, Object> linkedHashMapOf(final Entry<String, Object>... entries) {
		final Map<String, Object> all = new LinkedHashMap<String, Object>();
		for (final Entry<String, Object> entry : entries) {
			all.put(entry.getKey(), entry.getValue());
		}
		return all;
	}

	public static Map<String, Object> treeMapOf(final Map<String, Object> map) {
		final Map<String, Object> treeMap = new TreeMap<String, Object>(map);
		return treeMap;
	}

	public static Entry<String, Object> entry(final String name, final Object value) {
		return new AbstractMap.SimpleImmutableEntry<String, Object>(name, value);
	}

}
