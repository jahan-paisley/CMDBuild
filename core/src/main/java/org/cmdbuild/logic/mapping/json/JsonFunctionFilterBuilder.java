package org.cmdbuild.logic.mapping.json;

import static org.cmdbuild.logic.mapping.json.Constants.Filters.FUNCTION_NAME_KEY;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.driver.postgres.Const.SystemAttributes;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.QueryAliasAttribute;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import static org.cmdbuild.dao.query.clause.where.WhereClauses.*;
import org.cmdbuild.dao.view.CMDataView;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonFunctionFilterBuilder implements Builder<WhereClause> {

	private final JSONObject filterObject;
	private final CMEntryType entryType;
	private final QueryAliasAttribute attribute;
	private final OperationUser operationUser;

	public JsonFunctionFilterBuilder(final JSONObject filter, final CMEntryType entryType, final CMDataView dataView,
			final OperationUser operationUser) {
		Validate.notNull(filter, "invalid filter");
		Validate.notNull(entryType, "invalid entry type");
		Validate.notNull(dataView, "invalid data view");
		this.entryType = entryType;
		this.filterObject = filter;
		this.attribute = QueryAliasAttribute.attribute(entryType, SystemAttributes.Id.getDBName());
		this.operationUser = operationUser;
	}

	@Override
	public WhereClause build() {
		try {
			return buildWhereClause(filterObject);
		} catch (final JSONException e) {
			throw new IllegalArgumentException("malformed filter", e);
		}
	}

	private WhereClause buildWhereClause(final JSONObject filterObject) throws JSONException {
		CMEntryType entryType = this.entryType;
		if (filterObject.has(FUNCTION_NAME_KEY)) {
			final String name = filterObject.getString(FUNCTION_NAME_KEY);
			final Long userId = (operationUser == null) ? null : operationUser.getAuthenticatedUser().getId();
			final Long roleId = (operationUser == null) ? null : operationUser.getPreferredGroup().getId();
			return function(attribute, name, userId, roleId, entryType);
		}
		throw new IllegalArgumentException("The filter is malformed");
	}

}
