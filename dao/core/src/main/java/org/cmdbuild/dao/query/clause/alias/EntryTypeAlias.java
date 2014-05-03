package org.cmdbuild.dao.query.clause.alias;

import org.cmdbuild.dao.entrytype.CMEntryType;

public class EntryTypeAlias implements Alias {

	public static EntryTypeAlias canonicalAlias(final CMEntryType entryType) {
		return new EntryTypeAlias(entryType);
	}

	private final CMEntryType entryType;

	public EntryTypeAlias(final CMEntryType entryType) {
		this.entryType = entryType;
	}

	@Override
	public void accept(final AliasVisitor visitor) {
		visitor.visit(this);
	}

	public CMEntryType getEntryType() {
		return entryType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entryType == null) ? 0 : entryType.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof EntryTypeAlias)) {
			return false;
		}
		final EntryTypeAlias other = EntryTypeAlias.class.cast(obj);
		return (entryType == null) ? (other.entryType == null) : entryType.equals(other.entryType);
	}

	@Override
	public String toString() {
		if (entryType != null) {
			return entryType.getName();
		}
		return "";
	}

}
