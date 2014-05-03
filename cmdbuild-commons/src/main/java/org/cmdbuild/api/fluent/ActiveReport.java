package org.cmdbuild.api.fluent;

abstract class ActiveReport extends Report {

	private final FluentApi api;

	ActiveReport(final FluentApi api, final String title, final String format) {
		super(title, format);
		this.api = api;
	}

	protected FluentApi getApi() {
		return api;
	}

}
