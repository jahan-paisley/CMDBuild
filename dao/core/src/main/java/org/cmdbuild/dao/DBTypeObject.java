package org.cmdbuild.dao;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMIdentifier;

public abstract class DBTypeObject implements CMTypeObject {

	private final CMIdentifier identifier;
	private final Long id;

	protected DBTypeObject(final CMIdentifier identifier, final Long id) {
		Validate.notNull(identifier);
		Validate.notEmpty(identifier.getLocalName());
		this.identifier = identifier;
		this.id = id;
	}

	@Override
	public CMIdentifier getIdentifier() {
		return identifier;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return getIdentifier().getLocalName();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof CMEntryType == false) {
			return false;
		}
		final CMEntryType other = CMEntryType.class.cast(obj);
		return this.id.equals(other.getId());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("name", identifier.getLocalName()) //
				.append("namespace", identifier.getNameSpace()) //
				.toString();
	}

}
