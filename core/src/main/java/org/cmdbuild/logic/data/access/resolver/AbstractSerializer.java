package org.cmdbuild.logic.data.access.resolver;

import java.util.Map;

import org.cmdbuild.dao.entry.CMEntry;
import org.cmdbuild.dao.entrytype.attributetype.NullAttributeTypeVisitor;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver.EntryFiller;

public abstract class AbstractSerializer<T extends CMEntry> extends NullAttributeTypeVisitor {

	protected Object rawValue;
	protected String attributeName;
	protected LookupStore lookupStore;
	protected EntryFiller<T> entryFiller;

	public void setRawValue(final Object rawValue) {
		this.rawValue = rawValue;
	}

	public void setAttributeName(final String attributeName) {
		this.attributeName = attributeName;
	}

	public void setLookupStore(final LookupStore lookupStore) {
		this.lookupStore = lookupStore;
	}

	public void setEntryFiller(final EntryFiller<T> entryFiller) {
		this.entryFiller = entryFiller;
	}

	protected void setAttribute(final String name, final Object value) {
		entryFiller.setValue(name, value);
	}

}
