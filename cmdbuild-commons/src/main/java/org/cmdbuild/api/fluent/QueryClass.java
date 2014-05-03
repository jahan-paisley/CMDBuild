package org.cmdbuild.api.fluent;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryClass extends ActiveCard {

	private final Set<String> requestedAttributes;
	private final Set<String> unmodifiableRequestedAttributes;

	QueryClass(final FluentApi api, final String className) {
		super(api, className, null);
		requestedAttributes = new HashSet<String>();
		unmodifiableRequestedAttributes = unmodifiableSet(requestedAttributes);
	}

	public QueryClass withCode(final String value) {
		super.setCode(value);
		return this;
	}

	public QueryClass withDescription(final String value) {
		super.setDescription(value);
		return this;
	}

	public QueryClass with(final String name, final Object value) {
		return withAttribute(name, value);
	}

	public QueryClass withAttribute(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public QueryClass limitAttributes(final String... names) {
		requestedAttributes.addAll(asList(names));
		return this;
	}

	public Set<String> getRequestedAttributes() {
		return unmodifiableRequestedAttributes;
	}

	public List<Card> fetch() {
		return api().getExecutor().fetchCards(this);
	}

}
