package org.cmdbuild.dao.query.clause.alias;

import org.apache.commons.lang3.Validate;

public class NameAlias implements Alias {

	private final String name;

	private NameAlias(final String name) {
		Validate.notEmpty(name);
		this.name = name;
	}

	@Override
	public void accept(final AliasVisitor visitor) {
		visitor.visit(this);
	}

	public String getName() {
		return name;
	}

	/*
	 * Syntactic sugar
	 */
	public static NameAlias as(final String name) {
		return new NameAlias(name);
	}

	/*
	 * Object overrides
	 */

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NameAlias)) {
			return false;
		}
		final NameAlias other = (NameAlias) obj;
		return this.name.equals(other.name);
	}

	@Override
	public String toString() {
		return name;
	}

}
