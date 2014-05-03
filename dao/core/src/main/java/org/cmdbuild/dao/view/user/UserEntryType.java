package org.cmdbuild.dao.view.user;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.dao.entrytype.CMEntryType;
import org.cmdbuild.dao.entrytype.CMEntryTypeVisitor;
import org.cmdbuild.dao.entrytype.ForwardingEntryType;

public abstract class UserEntryType extends ForwardingEntryType {

	protected final UserDataView view;

	/*
	 * Should be used by the subclasses only
	 */
	protected UserEntryType(final CMEntryType inner, final UserDataView view) {
		super(inner);
		this.view = view;
	}

	@Override
	public Iterable<UserAttribute> getActiveAttributes() {
		return view.proxyAttributes(super.getActiveAttributes());
	}

	@Override
	public Iterable<UserAttribute> getAttributes() {
		return view.proxyAttributes(super.getAttributes());
	}

	@Override
	public Iterable<? extends CMAttribute> getAllAttributes() {
		return view.proxyAttributes(super.getAllAttributes());
	}

	@Override
	public UserAttribute getAttribute(final String name) {
		return view.proxy(super.getAttribute(name));
	}

	@Override
	public final void accept(final CMEntryTypeVisitor visitor) {
		super.accept(visitor);
	}

}
