package org.cmdbuild.api.fluent;

abstract class ActiveCard extends Card {

	private final FluentApi api;

	ActiveCard(final FluentApi api, final String className, final Integer id) {
		super(className, id);
		this.api = api;
	}

	protected FluentApi api() {
		return api;
	}

}
