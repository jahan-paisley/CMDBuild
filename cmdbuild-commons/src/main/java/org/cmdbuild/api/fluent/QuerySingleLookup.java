package org.cmdbuild.api.fluent;

public class QuerySingleLookup {

	private final FluentApi api;
	private final Integer id;

	QuerySingleLookup(final FluentApi api, final Integer id) {
		this.api = api;
		this.id = id;
	}

	public Lookup fetch() {
		return api.getExecutor().fetch(this);
	}

	public Integer getId() {
		return id;
	}

}
