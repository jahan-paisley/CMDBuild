package org.cmdbuild.api.fluent;

public class NewRelation extends ActiveRelation {

	NewRelation(final FluentApi api, final String domainName) {
		super(api, domainName);
	}

	public NewRelation withCard1(final String className, final int cardId) {
		super.setCard1(className, cardId);
		return this;
	}

	public NewRelation withCard2(final String className, final int cardId) {
		super.setCard2(className, cardId);
		return this;
	}

	public void create() {
		getApi().getExecutor().create(this);
	}

}
