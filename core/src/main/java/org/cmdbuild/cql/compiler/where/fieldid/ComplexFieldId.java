package org.cmdbuild.cql.compiler.where.fieldid;

import org.cmdbuild.cql.compiler.from.FromElement;

/**
 * A complex identifier for a field, e.g. Foo.Bar.Baz = 'foo' (where "Foo" is a
 * class alias) read:
 * "the Baz attribute from the object linked by Bar reference in the Foo class is equals to 'foo'"
 */
public class ComplexFieldId implements FieldId {
	FromElement from;
	String[] path;

	public ComplexFieldId(final String[] path, final FromElement from) {
		this.path = path;
		this.from = from;
	}

	public String[] getPath() {
		return path;
	}

	@Override
	public FromElement getFrom() {
		return from;
	}

	@Override
	public String getId() {
		return path[0];
	}
}
