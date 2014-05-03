package org.cmdbuild.api.fluent;

public class QueryAllLookup {

	private final FluentApi api;
	private final String type;

	QueryAllLookup(final FluentApi api, final String type) {
		this.api = api;
		this.type = type;
	}

	public Iterable<Lookup> fetch() {
		return api.getExecutor().fetch(this);
	}

	public QuerySingleLookup elementWithId(final Integer id) {
		return new QuerySingleLookup(api, id);
	}

	public String getType() {
		return type;
	}

}
