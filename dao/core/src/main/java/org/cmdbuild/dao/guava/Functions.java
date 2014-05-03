package org.cmdbuild.dao.guava;

import org.cmdbuild.dao.entry.CMCard;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.query.CMQueryRow;

import com.google.common.base.Function;

public class Functions {

	public static Function<CMQueryRow, CMCard> toCard(final CMClass type) {
		return new Function<CMQueryRow, CMCard>() {

			@Override
			public CMCard apply(final CMQueryRow input) {
				return input.getCard(type);
			}

		};
	}

	public static <T> Function<CMCard, T> toAttribute(final String name, final Class<T> type) {
		return new Function<CMCard, T>() {

			@Override
			public T apply(final CMCard input) {
				return input.get(name, type);
			}

		};
	}

	private Functions() {
		// prevents instantiation
	}

}
