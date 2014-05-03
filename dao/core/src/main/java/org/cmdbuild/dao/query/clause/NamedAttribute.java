package org.cmdbuild.dao.query.clause;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class NamedAttribute implements QueryAttribute {

	private final String entryTypeAliasName;
	private final String name;
	private transient final String toString;

	public NamedAttribute(final String fullname) {
		final String[] split = fullname.split("\\.");
		switch (split.length) {
		case 1:
			entryTypeAliasName = null;
			name = split[0];
			break;
		case 2:
			entryTypeAliasName = split[0];
			name = split[1];
			break;
		default:
			throw new IllegalArgumentException();
		}
		toString = ToStringBuilder.reflectionToString(this);
	}

	@Override
	public final String getName() {
		return name;
	}

	public final String getEntryTypeAliasName() {
		return entryTypeAliasName;
	}

	@Override
	public String toString() {
		return toString;
	}

}
