package org.cmdbuild.cmdbf;

import java.util.Set;

import com.google.common.base.Predicate;

public class IdConstraintPredicate implements Predicate<CMDBfId> {
	private final Set<CMDBfId> idSet;

	public IdConstraintPredicate(final Set<CMDBfId> idSet) {
		this.idSet = idSet;
	}

	@Override
	public boolean apply(final CMDBfId input) {
		return idSet.contains(input);
	}

}
