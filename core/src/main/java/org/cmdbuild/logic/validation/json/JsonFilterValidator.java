package org.cmdbuild.logic.validation.json;

import static com.google.common.collect.Iterators.contains;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.AND_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FUNCTION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARDS_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_CLASSNAME_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DESTINATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DOMAIN_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_SOURCE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ANY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_NOONE;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ONEOF;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.SIMPLE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.logic.validation.Validator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonFilterValidator implements Validator {

	private static final String MALFORMED_MSG = "Malformed filter";
	private final JSONObject filterObject;

	public JsonFilterValidator(final JSONObject filter) {
		try {
			Validate.notNull(filter);
			this.filterObject = filter;
		} catch (final Exception e) {
			throw new ValidationError(e);
		}
	}

	@Override
	public void validate() throws ValidationError {
		try {
			final boolean isEmptyFilter = !filterObject.keys().hasNext();
			if (!isEmptyFilter) {
				validateInnerFilterObjects();
			}
		} catch (final Exception e) {
			throw new ValidationError(e);
		}
	}

	private void validateInnerFilterObjects() throws Exception {
		Validate.isTrue(hasOneOfKeys(filterObject, //
				ATTRIBUTE_KEY, FULL_TEXT_QUERY_KEY, RELATION_KEY, CQL_KEY, FUNCTION_KEY), MALFORMED_MSG);
		validateAttributeFilter(filterObject);
		validateQueryFilter(filterObject);
		validateRelationFilter(filterObject);
		validateCQLFilter(filterObject);
	}

	private void validateAttributeFilter(final JSONObject filterObject) throws JSONException {
		if (filterObject.has(ATTRIBUTE_KEY)) {
			final JSONObject attributeFilter = filterObject.getJSONObject(ATTRIBUTE_KEY);
			Validate.isTrue(hasOneOfKeys(attributeFilter, SIMPLE_KEY, AND_KEY, OR_KEY), MALFORMED_MSG);
			if (attributeFilter.has(SIMPLE_KEY)) {
				final JSONObject simpleClauseObject = attributeFilter.getJSONObject(SIMPLE_KEY);
				Validate.isTrue(hasAllKeys(simpleClauseObject, //
						ATTRIBUTE_KEY, OPERATOR_KEY, VALUE_KEY), MALFORMED_MSG);
				validateSimpleClauseValues(simpleClauseObject);
			}
		}
	}

	private void validateSimpleClauseValues(final JSONObject simpleClauseObject) throws JSONException {
		simpleClauseObject.getJSONArray(VALUE_KEY);
	}

	private void validateQueryFilter(final JSONObject filterObject) throws JSONException {
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			filterObject.getString(FULL_TEXT_QUERY_KEY);
		}
	}

	private void validateRelationFilter(final JSONObject filterObject) throws JSONException {
		if (filterObject.has(RELATION_KEY)) {
			final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
			for (int i = 0; i < conditions.length(); i++) {
				final JSONObject condition = conditions.getJSONObject(i);
				Validate.isTrue(hasAllKeys(condition, //
						RELATION_DOMAIN_KEY, RELATION_SOURCE_KEY, RELATION_DESTINATION_KEY, RELATION_TYPE_KEY), //
						MALFORMED_MSG);
				Validate.isTrue(isNotBlank((String) condition.get(RELATION_DOMAIN_KEY)), MALFORMED_MSG);
				Validate.isTrue(isNotBlank((String) condition.get(RELATION_SOURCE_KEY)), MALFORMED_MSG);
				Validate.isTrue(isNotBlank((String) condition.get(RELATION_DESTINATION_KEY)), MALFORMED_MSG);
				Validate.isTrue(asList(RELATION_TYPE_ANY, RELATION_TYPE_NOONE, RELATION_TYPE_ONEOF) //
						.contains(condition.get(RELATION_TYPE_KEY)), MALFORMED_MSG);
				if (RELATION_TYPE_ONEOF.equals(condition.get(RELATION_TYPE_KEY))) {
					Validate.isTrue(condition.has(RELATION_CARDS_KEY), MALFORMED_MSG);
					final JSONArray cards = condition.getJSONArray(RELATION_CARDS_KEY);
					Validate.isTrue(cards.length() > 0);
					for (int j = 0; j < cards.length(); j++) {
						final JSONObject card = cards.getJSONObject(j);
						Validate.isTrue(hasAllKeys(card, //
								RELATION_CARD_ID_KEY, RELATION_CARD_CLASSNAME_KEY), //
								MALFORMED_MSG);
						Validate.isTrue(card.getInt(RELATION_CARD_ID_KEY) > 0, MALFORMED_MSG);
						Validate.isTrue(isNotBlank((String) card.get(RELATION_CARD_CLASSNAME_KEY)), MALFORMED_MSG);
					}
				}
			}
		}
	}

	private void validateCQLFilter(final JSONObject filterObject) {
		// empty until CQL filter will be implemented
	}

	private boolean hasOneOfKeys(final JSONObject jsonObject, final String... keys) {
		return hasOneOfKeys(jsonObject, asList(keys));
	}

	private boolean hasOneOfKeys(final JSONObject jsonObject, final List<String> keys) {
		final Iterator keysIterator = jsonObject.keys();
		while (keysIterator.hasNext()) {
			final String key = (String) keysIterator.next();
			if (keys.contains(key)) {
				return true;
			}
		}
		return false;
	}

	private boolean hasAllKeys(final JSONObject jsonObject, final String... keys) {
		return hasAllKeys(jsonObject, asList(keys));
	}

	private boolean hasAllKeys(final JSONObject jsonObject, final List<String> keys) {
		for (final String key : keys) {
			if (!contains(jsonObject.keys(), key)) {
				return false;
			}
		}
		return true;
	}

}
