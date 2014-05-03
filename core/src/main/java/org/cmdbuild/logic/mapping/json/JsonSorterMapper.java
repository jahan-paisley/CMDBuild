package org.cmdbuild.logic.mapping.json;

import static org.cmdbuild.dao.query.clause.QueryAliasAttribute.attribute;
import static org.cmdbuild.logic.mapping.json.Constants.DIRECTION_KEY;
import static org.cmdbuild.logic.mapping.json.Constants.PROPERTY_KEY;

import java.util.List;

import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.OrderByClause;
import org.cmdbuild.dao.query.clause.OrderByClause.Direction;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.logic.mapping.SorterMapper;
import org.cmdbuild.logic.validation.Validator;
import org.cmdbuild.logic.validation.json.JsonSorterValidator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.Lists;

public class JsonSorterMapper implements SorterMapper {

	private final CMEntryType entryType;
	private final JSONArray sorters;
	private final Validator validator;
	private final Alias entryTypeAlias;

	public JsonSorterMapper( //
			final CMEntryType entryType, //
			final JSONArray sorters, //
			final Alias alias //
	) {

		this.entryType = entryType;
		this.sorters = sorters;
		this.validator = new JsonSorterValidator(sorters);
		this.entryTypeAlias = alias;
	}

	public JsonSorterMapper(final CMEntryType entryType, final JSONArray sorters) {
		this(entryType, sorters, null);
	}

	@Override
	public List<OrderByClause> deserialize() {
		final List<OrderByClause> orderByClauses = Lists.newArrayList();
		if (sorters == null || sorters.length() == 0) {
			return orderByClauses;
		}
		validator.validate();
		for (int i = 0; i < sorters.length(); i++) {
			try {
				final JSONObject sorterObject = sorters.getJSONObject(i);
				final String attribute = sorterObject.getString(PROPERTY_KEY);
				final String direction = sorterObject.getString(DIRECTION_KEY);
				final QueryAliasAttribute queryAliasAttribute = buildQueryAliasAttribute(attribute);
				orderByClauses.add(new OrderByClause(queryAliasAttribute, Direction.valueOf(direction)));
			} catch (final JSONException ex) {
				throw new IllegalArgumentException("Malformed sorter");
			}
		}
		return orderByClauses;
	}

	private QueryAliasAttribute buildQueryAliasAttribute(final String attribute) {
		final QueryAliasAttribute queryAliasAttribute;

		if (entryTypeAlias == null) {
			queryAliasAttribute = attribute(entryType, attribute);
		} else {
			queryAliasAttribute = attribute(entryTypeAlias, attribute);
		}
		return queryAliasAttribute;
	}

}
