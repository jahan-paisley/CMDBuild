package org.cmdbuild.core.api.fluent;

import org.cmdbuild.api.fluent.Lookup;

public class LookupWrapper implements Lookup {

	private final org.cmdbuild.data.store.lookup.Lookup inner;

	public LookupWrapper(final org.cmdbuild.data.store.lookup.Lookup inner) {
		this.inner = inner;
	}

	@Override
	public String getType() {
		return inner.type.name;
	}

	@Override
	public String getCode() {
		return inner.code;
	}

	@Override
	public String getDescription() {
		return inner.getDescription();
	}

	@Override
	public Integer getId() {
		return inner.getId().intValue();
	}

}
