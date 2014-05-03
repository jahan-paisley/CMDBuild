package org.cmdbuild.api.fluent;

abstract class ActiveRelation extends Relation {

	private final FluentApi api;

	ActiveRelation(final FluentApi api, final String domainName) {
		super(domainName);
		this.api = api;
	}

	protected FluentApi getApi() {
		return api;
	}

}
