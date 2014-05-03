package org.cmdbuild.logic.data.lookup;

import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.cmdbuild.data.store.lookup.Lookup;
import org.cmdbuild.data.store.lookup.LookupType;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

class Util {

	private static final Logger logger = LookupLogic.logger;

	private Util() {
		// prevents instantiation
	}

	public static final Function<Lookup, LookupType> toLookupType() {
		logger.debug("converting from lookups to lookup types");
		return new Function<Lookup, LookupType>() {

			@Override
			public LookupType apply(final Lookup input) {
				return input.type;
			}

		};
	}

	public static Predicate<LookupType> uniques() {
		logger.debug("filtering unique lookup types");
		return new Predicate<LookupType>() {

			private final Set<LookupType> uniques = Sets.newHashSet();

			@Override
			public boolean apply(final LookupType input) {
				return uniques.add(input);
			}

		};
	}

	public static Predicate<LookupType> typesWith(final String name) {
		logger.debug("filtering lookup types with name '{}'");
		return new Predicate<LookupType>() {

			@Override
			public boolean apply(final LookupType input) {
				return input.name.equals(name);
			}

		};
	}

	public static Predicate<LookupType> typesWith(final String name, final String parent) {
		logger.debug("filtering lookup types with name '{}' and parent '{}'", name, parent);
		return new Predicate<LookupType>() {

			@Override
			public boolean apply(final LookupType input) {
				return new EqualsBuilder() //
						.append(name, input.name) //
						.append(parent, input.parent) //
						.isEquals();
			}

		};
	}

	public static Predicate<Lookup> withId(final Long id) {
		logger.debug("filtering lookups with id '{}'", id);
		return new Predicate<Lookup>() {
			@Override
			public boolean apply(final Lookup input) {
				return input.getId().equals(id);
			}
		};
	}

	public static Predicate<Lookup> actives(final boolean activeOnly) {
		logger.debug("filtering actives lookups (actives only '{}')", activeOnly);
		return new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return !activeOnly || input.active;
			}

		};
	}

	public static Predicate<Lookup> limited(final int start, final int limit) {
		logger.debug("filtering lookups starting at '{}' and limited at '{}'", start, limit);
		return new Predicate<Lookup>() {

			private final int end = limit > 0 ? limit + start : Integer.MAX_VALUE;
			private int i = 0;

			@Override
			public boolean apply(final Lookup input) {
				i++;
				return (start <= i && i < end);
			}

		};
	}
}
