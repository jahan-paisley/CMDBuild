package org.cmdbuild.dao.entry;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public abstract class ForwardingAttribute implements CMAttribute {

	private final CMAttribute delegate;

	protected ForwardingAttribute(final CMAttribute delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean isActive() {
		return delegate.isActive();
	}

	@Override
	public CMEntryType getOwner() {
		return delegate.getOwner();
	}

	@Override
	public CMAttributeType<?> getType() {
		return delegate.getType();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public boolean isSystem() {
		return delegate.isSystem();
	}

	@Override
	public boolean isInherited() {
		return delegate.isInherited();
	}

	@Override
	public boolean isDisplayableInList() {
		return delegate.isDisplayableInList();
	}

	@Override
	public boolean isMandatory() {
		return delegate.isMandatory();
	}

	@Override
	public boolean isUnique() {
		return delegate.isUnique();
	}

	@Override
	public Mode getMode() {
		return delegate.getMode();
	}

	@Override
	public int getIndex() {
		return delegate.getIndex();
	}

	@Override
	public String getDefaultValue() {
		return delegate.getDefaultValue();
	}

	@Override
	public String getGroup() {
		return delegate.getGroup();
	}

	@Override
	public int getClassOrder() {
		return delegate.getClassOrder();
	}

	@Override
	public String getEditorType() {
		return delegate.getEditorType();
	}

	@Override
	public String getFilter() {
		return delegate.getFilter();
	}

	@Override
	public String getForeignKeyDestinationClassName() {
		return delegate.getForeignKeyDestinationClassName();
	}

	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		return delegate.equals(obj);
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

}
