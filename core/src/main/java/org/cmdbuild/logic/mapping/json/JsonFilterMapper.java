package org.cmdbuild.logic.mapping.json;

import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.query.clause.alias.Alias;
import org.cmdbuild.dao.query.clause.where.WhereClause;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.mapping.FilterMapper;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class JsonFilterMapper implements FilterMapper {

	private static final Logger logger = Log.CMDBUILD;
	private static final Marker marker = MarkerFactory.getMarker(JsonFilterMapper.class.getName());

	public static class JsonFilterMapperBuilder implements Builder<JsonFilterMapper> {

		private static final Marker marker = MarkerFactory.getMarker(JsonFilterMapperBuilder.class.getName());

		private CMDataView dataView;
		private CMEntryType entryType;
		private Alias entryTypeAlias;
		private JSONObject filterObject;
		private OperationUser operationUser;

		private FilterMapper inner;

		@Override
		public JsonFilterMapper build() {
			Validate.notNull(entryType);
			Validate.notNull(filterObject);
			if (filterObject.has(CQL_KEY)) {
				throw new UnsupportedOperationException();
			} else {
				logger.info(marker, "filter is advanced filter");
				inner = new JsonAdvancedFilterMapper(entryType, filterObject, dataView, entryTypeAlias, operationUser);
			}
			return new JsonFilterMapper(this);
		}

		public JsonFilterMapperBuilder withDataView(final CMDataView dataView) {
			this.dataView = dataView;
			return this;
		}

		public JsonFilterMapperBuilder withEntryType(final CMEntryType entryType) {
			this.entryType = entryType;
			return this;
		}

		public JsonFilterMapperBuilder withEntryTypeAlias(final Alias entryTypeAlias) {
			this.entryTypeAlias = entryTypeAlias;
			return this;
		}

		public JsonFilterMapperBuilder withFilterObject(final JSONObject filterObject) {
			this.filterObject = filterObject;
			return this;
		}

		public JsonFilterMapperBuilder withOperationUser(final OperationUser operationUser) {
			this.operationUser = operationUser;
			return this;
		}

	}

	public static JsonFilterMapperBuilder newInstance() {
		return new JsonFilterMapperBuilder();
	}

	private final FilterMapper inner;

	private JsonFilterMapper(final JsonFilterMapperBuilder builder) {
		this.inner = builder.inner;
	}

	@Override
	public CMEntryType entryType() {
		logger.info(marker, "getting entry type");
		return inner.entryType();
	}

	@Override
	public Iterable<WhereClause> whereClauses() {
		logger.info(marker, "getting where clause");
		return inner.whereClauses();
	}

	@Override
	public Iterable<JoinElement> joinElements() {
		logger.info(marker, "getting join elements type");
		return inner.joinElements();
	}

}
