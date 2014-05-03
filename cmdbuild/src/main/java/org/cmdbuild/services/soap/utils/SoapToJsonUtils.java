package org.cmdbuild.services.soap.utils;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.cmdbuild.logic.mapping.json.Constants.DIRECTION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.PROPERTY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.AND_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.SIMPLE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.LookupAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.ReferenceAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.data.store.lookup.LookupType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.json.Constants;
import org.cmdbuild.services.soap.types.Attribute;
import org.cmdbuild.services.soap.types.CQLQuery;
import org.cmdbuild.services.soap.types.Filter;
import org.cmdbuild.services.soap.types.FilterOperator;
import org.cmdbuild.services.soap.types.Order;
import org.cmdbuild.services.soap.types.Query;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

/**
 * Represents a mapper used to convert from {@link Attribute} to
 * {@link JSONArray} and vice versa
 */
public class SoapToJsonUtils {

	private static final Logger logger = Log.SOAP;

	private static enum LogicalOperatorMapper {
		AND(AND_KEY), //
		OR(OR_KEY), //
		;

		private final String json;

		private LogicalOperatorMapper(final String json) {
			this.json = json;
		}

		public String getJson() {
			return json;
		}

		public static LogicalOperatorMapper of(final String s) {
			for (final LogicalOperatorMapper value : values()) {
				if (value.json.equalsIgnoreCase(s)) {
					return value;
				}
			}
			throw new IllegalArgumentException();
		}

	}

	private static enum SimpleOperatorMapper {

		EQUALS(Constants.FilterOperator.EQUAL), //
		LIKE(Constants.FilterOperator.LIKE), //

		UNDEFINED(null), //
		;

		private final Constants.FilterOperator filterOperator;

		private SimpleOperatorMapper(final Constants.FilterOperator filterOperator) {
			this.filterOperator = filterOperator;
		}

		public String getJson() {
			return (filterOperator == null) ? name() : filterOperator.toString();
		}

		public static SimpleOperatorMapper of(final String s) {
			for (final SimpleOperatorMapper value : values()) {
				if (value == null) {
					continue;
				}
				if (value.name().equalsIgnoreCase(s)) {
					return value;
				}
			}
			logger.warn("operator mapper not found for '{}'", s);
			return UNDEFINED;
		}

	}

	private SoapToJsonUtils() {
		// prevents instantiation
	}

	/**
	 * 
	 * @param attributes
	 *            an array of Attribute objects (SOAP)
	 * @return a JSONArray containing the names of the attributes
	 */
	public static JSONArray toJsonArray(final Attribute[] attributes) {
		final JSONArray jsonArray = new JSONArray();
		if (attributes != null) {
			for (final Attribute soapAttribute : attributes) {
				jsonArray.put(soapAttribute.getName());
			}
		}
		return jsonArray;

	}

	public static JSONArray toJsonArray(final Order[] sorters, final Attribute[] attributesSubsetForSelect) {
		final JSONArray jsonArray = new JSONArray();
		if (sorters != null) {
			for (final Order order : sorters) {
				final JSONObject object = new JSONObject();
				try {
					object.put(PROPERTY_KEY, order.getColumnName());
					object.put(DIRECTION_KEY, order.getType());
				} catch (final JSONException e) {
					// empty
				}
				jsonArray.put(object);
			}
		} else {
			if (attributesSubsetForSelect != null && attributesSubsetForSelect.length > 0) {
				final Attribute attributeUsedForOrder = attributesSubsetForSelect[0];
				final JSONObject object = new JSONObject();
				try {
					object.put(PROPERTY_KEY, attributeUsedForOrder.getName());
					object.put(DIRECTION_KEY, "ASC");
				} catch (final JSONException e) {
					// empty
				}
				jsonArray.put(object);
			}
		}
		return jsonArray;
	}

	public static JSONObject createJsonFilterFrom(final Query queryType, final String fullTextQuery,
			final CQLQuery cqlQuery, final CMClass targetClass, final LookupStore lookupStore) {
		final JSONObject filterObject = new JSONObject();
		try {
			if (queryType != null) {
				final JSONObject attributeFilterObject = jsonQuery(queryType, targetClass, lookupStore);
				if (attributeFilterObject != null) {
					filterObject.put(ATTRIBUTE_KEY, attributeFilterObject);
				}
			}
			if (StringUtils.isNotBlank(fullTextQuery)) {
				filterObject.put(FULL_TEXT_QUERY_KEY, fullTextQuery);
			}
			if (cqlQuery != null) {
				filterObject.put(CQL_KEY, cqlQuery.getCqlQuery());
			}
		} catch (final JSONException ex) {

		}
		return filterObject;
	}

	private static JSONObject jsonQuery(final Query query, final CMClass targetClass, final LookupStore lookupStore)
			throws JSONException {
		final JSONObject jsonObject = new JSONObject();
		final Filter filter = query.getFilter();
		final FilterOperator filterOperator = query.getFilterOperator();
		if (filter != null) {
			jsonObject.put(SIMPLE_KEY, jsonForFilter(targetClass, lookupStore, filter));
		} else if (filterOperator != null) {
			final String operator = LogicalOperatorMapper.of(filterOperator.getOperator()).getJson();
			final JSONArray jsonSubQueries = new JSONArray();
			for (final Query subQuery : filterOperator.getSubquery()) {
				jsonSubQueries.put(jsonQuery(subQuery, targetClass, lookupStore));
			}
			jsonObject.put(operator, jsonSubQueries);
		}
		return jsonObject;
	}

	private static JSONObject jsonForFilter(final CMClass targetClass, final LookupStore lookupStore, final Filter filter)
			throws JSONException {
		final String attributeToFilter = filter.getName();
		final JSONObject simple = new JSONObject();
		final List<Object> values = Lists.newArrayList();
		for (final String value : filter.getValue()) {
			final CMAttribute attribute = targetClass.getAttribute(attributeToFilter);
			final CMAttributeType<?> attributeType = (attribute == null) ? UndefinedAttributeType.undefined()
					: attribute.getType();
			if (attributeType instanceof LookupAttributeType) {
				final LookupAttributeType lookupAttributeType = LookupAttributeType.class.cast(attributeType);
				final String lookupTypeName = lookupAttributeType.getLookupTypeName();
				final Long lookupId;
				if (isNotBlank(value) && isNumeric(value)) {
					lookupId = lookupAttributeType.convertValue(value).getId();
				} else {
					// so it should be the description
					lookupId = findLookupIdByTypeAndValue(lookupTypeName, value, lookupStore);
				}
				values.add((lookupId == null) ? null : lookupId.toString());
			} else if (attributeType instanceof ReferenceAttributeType) {
				if (isNotBlank(value)) {
					values.add(value);
				}
			} else {
				if (attributeType.convertValue(value) != null) {
					values.add(value);
				}
			}
		}
		simple.put(ATTRIBUTE_KEY, attributeToFilter);
		if (values.isEmpty()) {
			simple.put(OPERATOR_KEY, Constants.FilterOperator.NULL.toString());
		} else if (values.size() == 1) {
			simple.put(OPERATOR_KEY, SimpleOperatorMapper.of(filter.getOperator()).getJson());
		} else {
			simple.put(OPERATOR_KEY, Constants.FilterOperator.IN.toString());
		}
		final JSONArray jsonValues = new JSONArray();
		for (final Object value : values) {
			jsonValues.put(value);
		}
		simple.put(VALUE_KEY, jsonValues);
		return simple;
	}

	private static Long findLookupIdByTypeAndValue( //
			final String lookupTypeName, //
			final String description, //
			final LookupStore lookupStore //
	) {
		final LookupType lookupType = LookupType.newInstance() //
				.withName(lookupTypeName) //
				.build();
		final Iterable<Lookup> lookupList = lookupStore.listForType(lookupType);
		for (final Lookup lookup : lookupList) {
			if (lookup.description.equals(description)) {
				return lookup.getId();
			}
		}
		logger.warn("lookup with description '{}' not found for within type '{}'", description, lookupTypeName);
		return 0L;
	}

}
