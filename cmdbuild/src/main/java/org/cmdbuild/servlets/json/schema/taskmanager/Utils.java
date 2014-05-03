package org.cmdbuild.servlets.json.schema.taskmanager;

import java.util.List;
import java.util.Map;

import org.cmdbuild.logger.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class Utils {

	private static final Logger logger = Log.JSONRPC;
	private static final Marker marker = MarkerFactory.getMarker(Utils.class.getName());

	private Utils() {
		// prevents instantiation
	}

	public static Map<String, String> toMap(final JSONObject json) {
		try {
			final Map<String, String> map = Maps.newHashMap();
			if (json != null && json.length() > 0) {
				for (final String key : JSONObject.getNames(json)) {
					map.put(key, json.getString(key));
				}
			}
			return map;
		} catch (final Exception e) {
			logger.warn(marker, "error parsing json data");
			throw new RuntimeException(e);
		}
	}

	public static Iterable<String> toIterable(final JSONArray json) {
		try {
			final List<String> values = Lists.newArrayList();
			if (json != null && json.length() > 0) {
				for (int index = 0; index < json.length(); index++) {
					values.add(json.getString(index));
				}
			}
			return values;
		} catch (final Exception e) {
			logger.warn(marker, "error parsing json data");
			throw new RuntimeException(e);
		}
	}

}
