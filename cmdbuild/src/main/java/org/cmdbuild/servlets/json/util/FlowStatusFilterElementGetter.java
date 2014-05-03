package org.cmdbuild.servlets.json.util;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.EQUAL;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.IN;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.servlets.json.util.JsonFilterHelper.FilterElementGetter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FlowStatusFilterElementGetter implements FilterElementGetter {

	private final LookupStore lookupStore;
	private final String flowStatus;

	public FlowStatusFilterElementGetter(final LookupStore lookupStore, final String flowStatus) {
		this.lookupStore = lookupStore;
		this.flowStatus = flowStatus;
	}

	@Override
	public boolean hasElement() {
		return !isBlank(flowStatus);
	}

	@Override
	public JSONObject getElement() throws JSONException {
		JsonFilterHelper.logger
				.debug(JsonFilterHelper.marker, "creating JSON flow status element for '{}'", flowStatus);
		final JSONArray singleValue = new JSONArray();
		final JSONArray allValues = new JSONArray();
		for (final Lookup element : lookupStore.listForType(LookupType.newInstance() //
				.withName("FlowStatus") //
				.build())) {
			if (element.code.equals(flowStatus)) {
				JsonFilterHelper.logger.debug(JsonFilterHelper.marker, "lookup found for flow status '{}'", flowStatus);
				singleValue.put(element.getId());
			}
			allValues.put(element.getId());
		}

		final JSONObject simple;
		simple = new JSONObject();
		simple.put(ATTRIBUTE_KEY, "FlowStatus");
		simple.put(OPERATOR_KEY, (singleValue.length() == 1) ? EQUAL : IN);
		simple.put(VALUE_KEY, (singleValue.length() == 1) ? singleValue : allValues);

		JsonFilterHelper.logger.debug(JsonFilterHelper.marker, "resulting element is '{}'", simple);

		return simple;
	}

}
