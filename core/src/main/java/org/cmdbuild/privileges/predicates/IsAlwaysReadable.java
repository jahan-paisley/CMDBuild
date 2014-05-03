package org.cmdbuild.privileges.predicates;

import org.cmdbuild.dao.entry.CMCard;

import com.google.common.base.Predicate;

public class IsAlwaysReadable implements Predicate<CMCard> {

	public IsAlwaysReadable() {
	}

	@Override
	public boolean apply(final CMCard input) {
		return true;
	}

}
