package org.cmdbuild.servlets.json.util;

import static org.cmdbuild.logic.mapping.json.Constants.Filters.AND_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.SIMPLE_KEY;

import org.cmdbuild.logger.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class JsonFilterHelper {

	protected static final Logger logger = Log.JSONRPC;
	protected static final Marker marker = MarkerFactory.getMarker(JsonFilterHelper.class.getName());

	public static interface FilterElementGetter {

		boolean hasElement();

		JSONObject getElement() throws JSONException;

	}

	private final JSONObject filter;

	public JsonFilterHelper(final JSONObject filter) {
		this.filter = filter;
	}

	public JSONObject merge(final FilterElementGetter filterElementGetter) throws JSONException {
		if (!filterElementGetter.hasElement()) {
			logger.debug(marker, "missing element");
			return filter;
		}

		final JSONObject additionalElement = filterElementGetter.getElement();
		logger.info(marker, "adding condition '{}' to actual filter '{}'", additionalElement, filter);

		final JSONObject alwaysValidJsonFilter = (filter == null) ? new JSONObject() : filter;

		final JSONObject attribute;
		if (alwaysValidJsonFilter.has(ATTRIBUTE_KEY)) {
			attribute = filter.getJSONObject(ATTRIBUTE_KEY);
		} else {
			logger.debug(marker, "filter has no element '{}' adding an empty one", ATTRIBUTE_KEY);
			attribute = new JSONObject();
			alwaysValidJsonFilter.put(ATTRIBUTE_KEY, attribute);
		}

		if (attribute.has(AND_KEY) || attribute.has(OR_KEY)) {
			logger.debug(marker, "attribute element has 'and' or 'or' sub-elements");
			final String key = attribute.has(AND_KEY) ? AND_KEY : OR_KEY;
			final JSONArray actual = attribute.getJSONArray(key);
			attribute.remove(key);
			final JSONArray arrayWithFlowStatus = new JSONArray();
			arrayWithFlowStatus.put(object(key, actual));
			arrayWithFlowStatus.put(simple(additionalElement));
			attribute.put(AND_KEY, arrayWithFlowStatus);
		} else if (attribute.has(SIMPLE_KEY)) {
			logger.debug(marker, "attribute element has 'simple' sub-element");
			final JSONObject actual = attribute.getJSONObject(SIMPLE_KEY);
			final JSONArray arrayWithFlowStatus = new JSONArray();
			arrayWithFlowStatus.put(simple(actual));
			arrayWithFlowStatus.put(simple(additionalElement));
			attribute.put(AND_KEY, arrayWithFlowStatus);
			attribute.remove(SIMPLE_KEY);
		} else {
			logger.debug(marker, "attribute element is empty");
			attribute.put(SIMPLE_KEY, additionalElement);
		}

		logger.debug(marker, "resulting filter is '{}'", alwaysValidJsonFilter);

		return alwaysValidJsonFilter;
	}

	private JSONObject simple(final JSONObject jsonObject) throws JSONException {
		return object(SIMPLE_KEY, jsonObject);
	}

	private JSONObject object(final String key, final JSONObject jsonObject) throws JSONException {
		return new JSONObject() {
			{
				put(key, jsonObject);
			}
		};
	}

	private JSONObject object(final String key, final JSONArray jsonArray) throws JSONException {
		return new JSONObject() {
			{
				put(key, jsonArray);
			}
		};
	}

}
