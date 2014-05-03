package org.cmdbuild.utils.guava;

import org.cmdbuild.dao.entrytype.CMEntryType;

import com.google.common.base.Function;

public final class Functions {

	private static final Function<CMEntryType, Long> CMENTRYTYPE_TO_ID = new Function<CMEntryType, Long>() {

		@Override
		public Long apply(final CMEntryType input) {
			return input.getId();
		}

	};

	private Functions() {
		// prevents instantiation
	}

	public static Function<CMEntryType, Long> id() {
		return CMENTRYTYPE_TO_ID;
	}

}
