package org.cmdbuild.servlets.json.serializers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.cmdbuild.dao.entrytype.CMClass;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * This class groups the history items by ID, order them by date (starting
 * with the most recent) and compares the values with the previous item to
 * mark the attributes that have changed
 */
public abstract class JsonHistory {

	protected static class ValueAndDescription {

		private final Object value;
		private final String description;

		public ValueAndDescription(final Object value, final String description) {
			this.value = value;
			this.description = description;
		}

		public Object getValue() {
			return value;
		}

		public String getDescription() {
			return description;
		}

	}

	protected interface HistoryItem {

		Long getId();

		long getInstant();

		Map<String, ValueAndDescription> getAttributes();

		Map<String, Object> getExtraAttributes();

		boolean isInOutput();

	}

	private static class ItemTimeline {

		private final List<HistoryItem> list = new ArrayList<HistoryItem>();

		public void addHistoryItem(final HistoryItem hi) {
			list.add(hi);
		}

		public Iterable<HistoryItem> getItems() {
			Collections.sort(list, new Comparator<HistoryItem>() {
				@Override
				public int compare(final HistoryItem hi1, final HistoryItem hi2) {
					return Long.signum(hi1.getInstant() - hi2.getInstant());
				}
			});
			return list;
		}

	}

	protected final CMClass targetClass;
	private final ItemTimeline timeline = new ItemTimeline();

	public JsonHistory(final CMClass targetClass) {
		this.targetClass = targetClass;
	}

	protected final void addHistoryItem(final HistoryItem historyItem) {
		timeline.addHistoryItem(historyItem);
	}

	public JSONArray toJson() throws JSONException {
		final JSONArray jsonArray = new JSONArray();
		addJsonHistoryItems(jsonArray);
		return jsonArray;
	}

	public void addJsonHistoryItems(final JSONArray jsonArray) throws JSONException {
		// for (final ItemTimeline timeline : itemsTimeline.values()) {
		HistoryItem previous = null;
		for (final HistoryItem current : timeline.getItems()) {
			if (current.isInOutput()) {
				jsonArray.put(historyItemToJson(current, previous));
			}
			previous = current;
		}
		// }
	}

	protected final JSONObject historyItemToJson(final HistoryItem current, final HistoryItem previous)
			throws JSONException {
		final JSONObject jsonHistoryItem = new JSONObject();
		for (final Map.Entry<String, Object> entry : current.getExtraAttributes().entrySet()) {
			jsonHistoryItem.put(entry.getKey(), entry.getValue());
		}
		jsonHistoryItem.put("Attr", historyItemAttributesToJson(current, previous));
		return jsonHistoryItem;
	}

	private JSONArray historyItemAttributesToJson(final HistoryItem current, final HistoryItem previous)
			throws JSONException {
		final JSONArray jsonAttr = new JSONArray();
		for (final Map.Entry<String, ValueAndDescription> entry : current.getAttributes().entrySet()) {
			final JSONObject jsonAttrValue = new JSONObject();
			final ValueAndDescription vad = entry.getValue();
			final Object currentValue = vad.getValue();
			final String currentDescription = vad.getDescription();
			jsonAttrValue.put("d", currentDescription);
			jsonAttrValue.put("v", currentValue);
			// Add changed field
			if (previous != null) {
				final ValueAndDescription valueAndDesc = previous.getAttributes().get(entry.getKey());
				if (valueAndDesc != null) {
					final Object previousValue = valueAndDesc.getValue();
					if (areTwoValuesDifferent(currentValue, previousValue)) {
						jsonAttrValue.put("c", true);
					}
				}
			}
			jsonAttr.put(jsonAttrValue);
		}
		return jsonAttr;
	}

	private boolean areTwoValuesDifferent(Object currentValue, Object previousValue) {
		if (currentValue instanceof JSONObject) {
			currentValue = currentValue.toString();
		}
		if (previousValue instanceof JSONObject) {
			previousValue = previousValue.toString();
		}
		return !ObjectUtils.equals(currentValue, previousValue);
	}
}
