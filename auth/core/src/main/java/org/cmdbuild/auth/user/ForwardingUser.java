package org.cmdbuild.auth.user;

import java.util.List;
import java.util.Set;

public abstract class ForwardingUser implements CMUser {

	private final CMUser delegate;

	protected ForwardingUser(final CMUser delegate) {
		this.delegate = delegate;
	}

	@Override
	public Long getId() {
		return delegate.getId();
	}

	@Override
	public String getUsername() {
		return delegate.getUsername();
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public Set<String> getGroupNames() {
		return delegate.getGroupNames();
	}

	@Override
	public List<String> getGroupDescriptions() {
		return delegate.getGroupDescriptions();
	}

	@Override
	public String getDefaultGroupName() {
		return delegate.getDefaultGroupName();
	}

	@Override
	public String getEmail() {
		return delegate.getEmail();
	}

	@Override
	public boolean isActive() {
		return delegate.isActive();
	}

}
