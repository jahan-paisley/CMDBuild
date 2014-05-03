package org.cmdbuild.cql.compiler.where.fieldid;

import org.cmdbuild.cql.compiler.from.FromElement;

/**
 * A simple field identifier is a 1 or 2 token identifier (2 if the first is a
 * class identifier), e.g. Foo.Bar, when "Foo" is a class name or class as, or
 * simply Bar, if Foo is the main class (ie. the first one referenced in the
 * FROM statement).
 */
public class SimpleFieldId implements FieldId {
	FromElement from;
	String id;

	public SimpleFieldId(final String id, final FromElement from) {
		this.id = id;
		this.from = from;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public FromElement getFrom() {
		return from;
	}
}
