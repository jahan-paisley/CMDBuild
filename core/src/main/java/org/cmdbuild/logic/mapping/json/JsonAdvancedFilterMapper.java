package org.cmdbuild.logic.mapping.json;

import static org.cmdbuild.dao.driver.postgres.Const.SystemAttributes.Id;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.IN;
import static org.cmdbuild.logic.mapping.json.Constants.FilterOperator.NULL;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.ATTRIBUTE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.CLASSNAME_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FULL_TEXT_QUERY_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.FUNCTION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.OPERATOR_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARDS_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_CARD_ID_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DESTINATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_DOMAIN_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_SOURCE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_NOONE;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.RELATION_TYPE_ONEOF;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.SIMPLE_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.Filters.VALUE_KEY;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.cmdbuild.logic.validation.Validator;
import org.cmdbuild.logic.validation.json.JsonFilterValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

public class JsonAdvancedFilterMapper implements FilterMapper {

	private static final Logger logger = Log.CMDBUILD;

	private static final JSONArray EMPTY_VALUES = new JSONArray();

	private final CMEntryType entryType;
	private final JSONObject filterObject;
	private final Validator filterValidator;
	private final CMDataView dataView;
	private final Alias entryTypeAlias;
	private final OperationUser operationUser;

	public JsonAdvancedFilterMapper(//
			final CMEntryType entryType, //
			final JSONObject filterObject, //
			final CMDataView dataView, //
			final Alias entryTypeAlias, //
			final OperationUser operationUser //
	) {
		Validate.notNull(entryType);
		Validate.notNull(filterObject);
		this.entryType = entryType;
		this.filterObject = filterObject;
		this.filterValidator = new JsonFilterValidator(filterObject);
		this.dataView = dataView;
		this.entryTypeAlias = entryTypeAlias;
		this.operationUser = operationUser;
	}

	public JsonAdvancedFilterMapper( //
			final CMEntryType entryType, //
			final JSONObject filterObject, //
			final CMDataView dataView, //
			final OperationUser operationUser //
	) {
		this(entryType, filterObject, dataView, null, operationUser);
	}

	@Override
	public CMEntryType entryType() {
		return entryType;
	}

	@Override
	public Iterable<WhereClause> whereClauses() {
		filterValidator.validate();

		Iterable<Builder<WhereClause>> whereClauseBuilders;
		try {
			whereClauseBuilders = getWhereClauseBuildersForFilter();
		} catch (final JSONException ex) {
			throw new IllegalArgumentException("malformed filter", ex);
		}

		final List<WhereClause> whereClauses = Lists.newArrayList();
		for (final Builder<WhereClause> builder : whereClauseBuilders) {
			whereClauses.add(builder.build());
		}
		return whereClauses;
	}

	private Iterable<Builder<WhereClause>> getWhereClauseBuildersForFilter() throws JSONException {
		final List<Builder<WhereClause>> whereClauseBuilders = Lists.newArrayList();
		if (filterObject.has(ATTRIBUTE_KEY)) {
			whereClauseBuilders.add(new JsonAttributeFilterBuilder(filterObject.getJSONObject(ATTRIBUTE_KEY),
					entryType, dataView));
		}
		if (filterObject.has(FULL_TEXT_QUERY_KEY)) {
			final JsonFullTextQueryBuilder jsonFullTextQueryBuilder = new JsonFullTextQueryBuilder( //
					filterObject.getString(FULL_TEXT_QUERY_KEY), //
					entryType, //
					entryTypeAlias);
			whereClauseBuilders.add(jsonFullTextQueryBuilder);
		}
		if (filterObject.has(RELATION_KEY)) {
			final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
			for (int i = 0; i < conditions.length(); i++) {
				final JSONObject condition = conditions.getJSONObject(i);
				if (condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_ONEOF)) {
					final JSONArray cards = condition.getJSONArray(RELATION_CARDS_KEY);

					final JSONObject simple = new JSONObject();
					simple.put(ATTRIBUTE_KEY, Id.getDBName());
					simple.put(OPERATOR_KEY, IN.toString());
					simple.put(CLASSNAME_KEY, condition.getString(RELATION_DESTINATION_KEY));

					final JSONObject filter = new JSONObject();
					filter.put(SIMPLE_KEY, simple);

					for (int j = 0; j < cards.length(); j++) {
						final JSONObject card = cards.getJSONObject(j);
						final Long id = card.getLong(RELATION_CARD_ID_KEY);
						simple.append(VALUE_KEY, id);
					}

					whereClauseBuilders.add(new JsonAttributeFilterBuilder(filter, entryType, dataView));
				} else if (condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_NOONE)) {
					final JSONObject simple = new JSONObject();
					simple.put(ATTRIBUTE_KEY, Id.getDBName());
					simple.put(OPERATOR_KEY, NULL.toString());
					simple.put(CLASSNAME_KEY, condition.getString(RELATION_DESTINATION_KEY));
					simple.put(VALUE_KEY, EMPTY_VALUES);

					final JSONObject filter = new JSONObject();
					filter.put(SIMPLE_KEY, simple);

					whereClauseBuilders.add(new JsonAttributeFilterBuilder(filter, entryType, dataView));

				}
			}
		}
		if (filterObject.has(FUNCTION_KEY)) {
			final JSONArray array = filterObject.getJSONArray(FUNCTION_KEY);
			for (int i = 0; i < array.length(); i++) {
				final JSONObject definition = array.getJSONObject(i);
				whereClauseBuilders.add(new JsonFunctionFilterBuilder(definition, entryType, dataView, operationUser));
			}
		}
		return whereClauseBuilders;
	}

	@Override
	public Iterable<JoinElement> joinElements() {
		logger.info("getting join elements for filter");
		final List<JoinElement> joinElements = Lists.newArrayList();
		if (filterObject.has(RELATION_KEY)) {
			try {
				final JSONArray conditions = filterObject.getJSONArray(RELATION_KEY);
				for (int i = 0; i < conditions.length(); i++) {
					final JSONObject condition = conditions.getJSONObject(i);
					final String domain = condition.getString(RELATION_DOMAIN_KEY);
					final String source = condition.getString(RELATION_SOURCE_KEY);
					final String destination = condition.getString(RELATION_DESTINATION_KEY);
					final boolean left = condition.getString(RELATION_TYPE_KEY).equals(RELATION_TYPE_NOONE);
					joinElements.add(JoinElement.newInstance(domain, source, destination, left));
				}
			} catch (final Exception e) {
				logger.error("error getting json element", e);
			}
		}
		return joinElements;
	}

}
