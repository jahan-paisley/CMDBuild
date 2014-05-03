package org.cmdbuild.logic.data;

import static org.cmdbuild.logic.mapping.json.Constants.Filters.CQL_KEY;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.common.Builder;
import org.cmdbuild.logger.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;

import com.google.common.collect.Maps;

/**
 * Simple DTO that represents the options for a query in CMDBuild
 */
public class QueryOptions {

	private static final Logger logger = Log.CMDBUILD;

	public static class QueryOptionsBuilder implements Builder<QueryOptions> {

		private int limit;
		private int offset;
		private JSONObject filter;
		private JSONArray sorters;
		private JSONArray attributeSubset;
		private Map<String, Object> parameters;

		private QueryOptionsBuilder() {
			limit = Integer.MAX_VALUE;
			offset = 0;
			filter = new JSONObject();
			sorters = new JSONArray();
			attributeSubset = new JSONArray();
			parameters = Maps.newHashMap();
		}

		public QueryOptionsBuilder limit(final int limit) {
			this.limit = limit;
			return this;
		}

		public QueryOptionsBuilder offset(final int offset) {
			this.offset = offset;
			return this;
		}

		public QueryOptionsBuilder orderBy(final JSONArray sorters) {
			if (sorters == null) {
				this.sorters = new JSONArray();
			} else {
				this.sorters = sorters;
			}
			return this;
		}

		public QueryOptionsBuilder filter(final JSONObject filter) {
			if (filter == null) {
				this.filter = new JSONObject();
			} else {
				this.filter = filter;
			}
			return this;
		}

		public QueryOptionsBuilder onlyAttributes(final JSONArray attributes) {
			if (attributes == null) {
				this.attributeSubset = new JSONArray();
			} else {
				this.attributeSubset = attributes;
			}
			return this;
		}

		public QueryOptionsBuilder parameters(final Map<String, Object> parameters) {
			this.parameters = parameters;
			return this;
		}

		public QueryOptionsBuilder clone(final QueryOptions queryOptions) {
			limit = queryOptions.limit;
			offset = queryOptions.offset;
			filter = queryOptions.filter;
			sorters = queryOptions.sorters;
			attributeSubset = queryOptions.attributes;
			parameters = queryOptions.parameters;
			return this;
		}

		@Override
		public QueryOptions build() {
			preReleaseHackToFixCqlFilters();
			if (offset == 0 && limit == 0) {
				limit = Integer.MAX_VALUE;
			}
			return new QueryOptions(this);
		}

		/*
		 * FIXME remove this and fix JavaScript ASAP
		 */
		private void preReleaseHackToFixCqlFilters() {
			try {
				final Map<String, Object> cqlParameters = Maps.newHashMap();
				boolean addParameters = false;
				for (final Entry<String, Object> entry : parameters.entrySet()) {
					final String key = entry.getKey();
					if (key.equals(CQL_KEY)) {
						filter.put(CQL_KEY, entry.getValue());
						addParameters = true;
					} else if (key.startsWith("p")) {
						cqlParameters.put(key, entry.getValue());
					}
				}
				if (addParameters) {
					for (final Entry<String, Object> entry : cqlParameters.entrySet()) {
						filter.put(entry.getKey(), entry.getValue());
					}
				}
			} catch (final Throwable e) {
				logger.error("error while hacking filter", e);
			}
		}
	}

	public static QueryOptionsBuilder newQueryOption() {
		return new QueryOptionsBuilder();
	}

	private final int limit;
	private final int offset;
	private final JSONObject filter;
	private final JSONArray sorters;
	private final JSONArray attributes;
	private final Map<String, Object> parameters;

	private QueryOptions(final QueryOptionsBuilder builder) {
		this.limit = builder.limit;
		this.offset = builder.offset;
		this.filter = builder.filter;
		this.sorters = builder.sorters;
		this.attributes = builder.attributeSubset;
		this.parameters = builder.parameters;
	}

	public int getLimit() {
		return limit;
	}

	public int getOffset() {
		return offset;
	}

	public JSONObject getFilter() {
		return filter;
	}

	public JSONArray getSorters() {
		return sorters;
	}

	public JSONArray getAttributes() {
		return attributes;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

}
