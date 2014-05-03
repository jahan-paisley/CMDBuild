package org.cmdbuild.dao.entrytype;

public abstract class ForwardingDomain extends ForwardingEntryType implements CMDomain {

	private final CMDomain delegate;

	protected ForwardingDomain(final CMDomain delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public CMClass getClass1() {
		return delegate.getClass1();
	}

	@Override
	public CMClass getClass2() {
		return delegate.getClass2();
	}

	@Override
	public String getDescription1() {
		return delegate.getDescription1();
	}

	@Override
	public String getDescription2() {
		return delegate.getDescription2();
	}

	@Override
	public String getCardinality() {
		return delegate.getCardinality();
	}

	@Override
	public boolean isMasterDetail() {
		return delegate.isMasterDetail();
	}

	@Override
	public String getMasterDetailDescription() {
		return delegate.getMasterDetailDescription();
	}

}
