package org.cmdbuild.api.fluent;

import java.util.ArrayList;
import java.util.List;

public class ActiveQueryRelations extends RelationsQuery {

	private final FluentApi api;

	ActiveQueryRelations(final FluentApi api, final String className, final Integer id) {
		super(className, id);
		this.api = api;
	}

	protected FluentApi api() {
		return api;
	}

	public ActiveQueryRelations withDomain(final String domainName) {
		setDomainName(domainName);
		return this;
	}

	public List<CardDescriptor> fetch() {
		final List<CardDescriptor> descriptors = new ArrayList<CardDescriptor>();
		final List<Relation> relations = api.getExecutor().fetch(this);
		for (final Relation relation : relations) {
			descriptors.add(descriptorFrom(relation));
		}
		return descriptors;
	}

	private CardDescriptor descriptorFrom(final Relation relation) {
		final String className;
		final int id;
		if (getCardId() == relation.getCardId1()) {
			className = relation.getClassName2();
			id = relation.getCardId2();
		} else {
			className = relation.getClassName1();
			id = relation.getCardId1();
		}
		return new CardDescriptor(className, id);
	}

}
