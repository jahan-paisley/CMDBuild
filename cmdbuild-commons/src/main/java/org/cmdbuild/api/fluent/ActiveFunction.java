package org.cmdbuild.api.fluent;

abstract class ActiveFunction extends Function {

	private final FluentApi api;

	ActiveFunction(final FluentApi api, final String functionName) {
		super(functionName);
		this.api = api;
	}

	protected FluentApi getApi() {
		return api;
	}

}
