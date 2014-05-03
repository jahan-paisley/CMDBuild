package org.cmdbuild.logic.mapping.json;

import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.size;
import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.dao.query.clause.where.AndWhereClause.and;
import static org.cmdbuild.dao.query.clause.where.BeginsWithOperatorAndValue.beginsWith;
import static org.cmdbuild.dao.query.clause.where.ContainsOperatorAndValue.contains;
import static org.cmdbuild.dao.query.clause.where.EndsWithOperatorAndValue.endsWith;
import static org.cmdbuild.dao.query.clause.where.EqualsOperatorAndValue.eq;
import static org.cmdbuild.dao.query.clause.where.GreaterThanOperatorAndValue.gt;
import static org.cmdbuild.dao.query.clause.where.InOperatorAndValue.in;
import static org.cmdbuild.dao.query.clause.where.LessThanOperatorAndValue.lt;
import static org.cmdbuild.dao.query.clause.where.NotWhereClause.not;
import static org.cmdbuild.dao.query.clause.where.NullOperatorAndValue.isNull;
import static org.cmdbuild.dao.query.clause.where.OrWhereClause.or;
import static org.cmdbuild.dao.query.clause.where.SimpleWhereClause.condition;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.AND_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CLASSNAME_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.SIMPLE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.IntegerAttributeType;
import org.cmdbuild.dao.entrytype.attributetype.UndefinedAttributeType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logic.mapping.json.Constants.FilterOperator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

/**
 * Class that creates a WhereClause starting from a json string. This where
 * clause will "retrieve" the cards of the specified entry type that match the
 * filter. It is used only for filter on attributes.
 */
public class JsonAttributeFilterBuilder implements Builder<WhereClause> {

	private final JSONObject filterObject;
	private final CMEntryType entryType;
	private final CMDataView dataViewForBuild;

	/**
	 * @param filter
	 *            the JSON object representing the filter.
	 * @param entryType
	 *            the entry type specified in the <code>from</code> clause.
	 * @param dataView
	 */
	public JsonAttributeFilterBuilder(final JSONObject filter, final CMEntryType entryType, final CMDataView dataView) {
		Validate.notNull(filter);
		Validate.notNull(entryType);
		Validate.notNull(dataView);
		this.entryType = entryType;
		this.filterObject = filter;
		this.dataViewForBuild = dataView;
	}

	@Override
	public WhereClause build() {
		try {
			return buildWhereClause(filterObject);
		} catch (final JSONException e) {
			throw new IllegalArgumentException("malformed filter", e);
		}
	}

	protected WhereClause buildWhereClause(final JSONObject filterObject) throws JSONException {
		CMEntryType entryType = this.entryType;
		if (filterObject.has(SIMPLE_KEY)) {
			final JSONObject condition = filterObject.getJSONObject(SIMPLE_KEY);
			final String attributeName = condition.getString(ATTRIBUTE_KEY);
			final String operator = condition.getString(OPERATOR_KEY);
			if (condition.has(CLASSNAME_KEY)) {
				entryType = dataViewForBuild.findClass(condition.getString(CLASSNAME_KEY));
			}
			final JSONArray jsonArray = condition.getJSONArray(VALUE_KEY);
			final List<Object> values = Lists.newArrayList();
			for (int i = 0; i < jsonArray.length(); i++) {
				values.add(jsonArray.getString(i));
			}
			return buildSimpleWhereClause(attribute(entryType, attributeName), operator, values);
		} else if (filterObject.has(AND_KEY)) {
			final JSONArray conditions = filterObject.getJSONArray(AND_KEY);
			Validate.isTrue(conditions.length() >= 2);
			final JSONObject first = conditions.getJSONObject(0);
			final JSONObject second = conditions.getJSONObject(1);
			return and(buildWhereClause(first), //
					buildWhereClause(second), //
					createOptionalWhereClauses(conditions));
		} else if (filterObject.has(OR_KEY)) {
			final JSONArray conditions = filterObject.getJSONArray(OR_KEY);
			Validate.isTrue(conditions.length() >= 2);
			final JSONObject first = conditions.getJSONObject(0);
			final JSONObject second = conditions.getJSONObject(1);
			return or(buildWhereClause(first), //
					buildWhereClause(second), //
					createOptionalWhereClauses(conditions));
		}
		throw new IllegalArgumentException("The filter is malformed");
	}

	/**
	 * NOTE: @parameter values is always an array of strings
	 */
	private WhereClause buildSimpleWhereClause(final QueryAliasAttribute attribute, final String operator,
			final Iterable<Object> values) throws JSONException {
		/**
		 * In this way if the user does not have privileges to read that
		 * attributes, it is possible to fetch it to build the correct where
		 * clause
		 */
		final CMEntryType dbEntryType = dataViewForBuild.findClass(entryType.getName());
		final CMAttribute a = dbEntryType.getAttribute(attribute.getName());
		final CMAttributeType<?> type = (a == null) ? UndefinedAttributeType.undefined() : a.getType();
		return buildSimpleWhereClause(attribute, operator, values, type);
	}

	private WhereClause buildSimpleWhereClause(final QueryAliasAttribute attribute, final String operator,
			final Iterable<Object> values, CMAttributeType<?> type) throws JSONException {
		if (attribute.getName().equals(SystemAttributes.Id.getDBName())) {
			type = new IntegerAttributeType();
		}

		if (operator.equals(FilterOperator.EQUAL.toString())) {
			Validate.isTrue(size(values) == 1);
			return condition(attribute, eq(type.convertValue(get(values, 0))));
		} else if (operator.equals(FilterOperator.NOT_EQUAL.toString())) {
			Validate.isTrue(size(values) == 1);
			return or(not(condition(attribute, eq(type.convertValue(get(values, 0))))), condition(attribute, isNull()));
		} else if (operator.equals(FilterOperator.NULL.toString())) {
			Validate.isTrue(size(values) == 0);
			return condition(attribute, isNull());
		} else if (operator.equals(FilterOperator.NOT_NULL.toString())) {
			Validate.isTrue(size(values) == 0);
			return not(condition(attribute, isNull()));
		} else if (operator.equals(FilterOperator.GREATER_THAN.toString())) {
			Validate.isTrue(size(values) == 1);
			return condition(attribute, gt(type.convertValue(get(values, 0))));
		} else if (operator.equals(FilterOperator.LESS_THAN.toString())) {
			Validate.isTrue(size(values) == 1);
			return condition(attribute, lt(type.convertValue(get(values, 0))));
		} else if (operator.equals(FilterOperator.BETWEEN.toString())) {
			Validate.isTrue(size(values) == 2);
			return and(condition(attribute, gt(type.convertValue(get(values, 0)))),
					condition(attribute, lt(type.convertValue(get(values, 1)))));
		} else if (operator.equals(FilterOperator.LIKE.toString())) {
			Validate.isTrue(size(values) == 1);
			return condition(attribute, contains(type.convertValue(get(values, 0))));
		} else if (operator.equals(FilterOperator.CONTAIN.toString())) {
			Validate.isTrue(size(values) == 1);
			return condition(attribute, contains(type.convertValue(get(values, 0))));
		} else if (operator.equals(FilterOperator.NOT_CONTAIN.toString())) {
			Validate.isTrue(size(values) == 1);
			return or(not(condition(attribute, contains(type.convertValue(get(values, 0))))),
					condition(attribute, isNull()));
		} else if (operator.equals(FilterOperator.BEGIN.toString())) {
			Validate.isTrue(size(values) == 1);
			return condition(attribute, beginsWith(type.convertValue(get(values, 0))));
		} else if (operator.equals(FilterOperator.NOT_BEGIN.toString())) {
			Validate.isTrue(size(values) == 1);
			return or(not(condition(attribute, beginsWith(type.convertValue(get(values, 0))))),
					condition(attribute, isNull()));
		} else if (operator.equals(FilterOperator.END.toString())) {
			Validate.isTrue(size(values) == 1);
			return condition(attribute, endsWith(type.convertValue(get(values, 0))));
		} else if (operator.equals(FilterOperator.NOT_END.toString())) {
			Validate.isTrue(size(values) == 1);
			return or(not(condition(attribute, endsWith(type.convertValue(get(values, 0))))),
					condition(attribute, isNull()));
		} else if (operator.equals(FilterOperator.IN.toString())) {
			Validate.isTrue(size(values) >= 1);
			final List<Object> _values = Lists.newArrayList();
			for (int i = 0; i < size(values); i++) {
				_values.add(type.convertValue(get(values, i)));
			}
			return condition(attribute, in(_values.toArray()));
		}
		throw new IllegalArgumentException("The operator " + operator + " is not supported");
	}

	private WhereClause[] createOptionalWhereClauses(final JSONArray conditions) throws JSONException {
		final List<WhereClause> optionalWhereClauses = Lists.newArrayList();
		for (int i = 2; i < conditions.length(); i++) {
			final JSONObject andCond = conditions.getJSONObject(i);
			optionalWhereClauses.add(buildWhereClause(andCond));
		}
		return optionalWhereClauses.toArray(new WhereClause[optionalWhereClauses.size()]);
	}

}
