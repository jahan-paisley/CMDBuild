package org.cmdbuild.dao.entrytype;

public abstract class ForwardingClass extends ForwardingEntryType implements CMClass {

	private final CMClass delegate;

	protected ForwardingClass(final CMClass delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public CMClass getParent() {
		return delegate.getParent();
	}

	@Override
	public Iterable<? extends CMClass> getChildren() {
		return delegate.getChildren();
	}

	@Override
	public Iterable<? extends CMClass> getLeaves() {
		return delegate.getLeaves();
	}

	@Override
	public Iterable<? extends CMClass> getDescendants() {
		return delegate.getDescendants();
	}

	@Override
	public boolean isAncestorOf(final CMClass cmClass) {
		return delegate.isAncestorOf(cmClass);
	}

	@Override
	public boolean isSuperclass() {
		return delegate.isSuperclass();
	}

	@Override
	public String getCodeAttributeName() {
		return delegate.getCodeAttributeName();
	}

	@Override
	public String getDescriptionAttributeName() {
		return delegate.getDescriptionAttributeName();
	}

	@Override
	public boolean isUserStoppable() {
		return delegate.isUserStoppable();
	}

	@Override
	public boolean isSimple() {
		return delegate.isSimple();
	}
}
