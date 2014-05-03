package org.cmdbuild.data.store.lookup;

import static com.google.common.collect.FluentIterable.from;
import static com.google.common.collect.Maps.newHashMap;

import java.util.List;
import java.util.Map;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;

public class DataViewLookupStore implements LookupStore {

	protected static final Marker marker = MarkerFactory.getMarker(DataViewLookupStore.class.getName());

	private final Store<Lookup> inner;

	public DataViewLookupStore(final Store<Lookup> store) {
		this.inner = store;
	}

	@Override
	public org.cmdbuild.data.store.Storable create(final Lookup storable) {
		return inner.create(storable);
	}

	@Override
	public Lookup read(final org.cmdbuild.data.store.Storable storable) {
		return inner.read(storable);
	}

	@Override
	public void update(final Lookup storable) {
		inner.update(storable);
	}

	@Override
	public void delete(final Storable storable) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<Lookup> list() {
		return inner.list();
	}

	@Override
	public List<Lookup> list(final Groupable groupable) {
		return inner.list(groupable);
	}

	@Override
	public Iterable<Lookup> listForType(final LookupType type) {
		logger.debug(marker, "getting lookups with type '{}'", type);

		final Iterable<Lookup> lookups = list();

		final Map<Long, Lookup> lookupsById = newHashMap();
		for (final Lookup lookup : lookups) {
			lookupsById.put(lookup.getId(), lookup);
		}

		for (final Lookup lookup : lookups) {
			final Lookup lookupWithParent = buildLookupWithParentLookup(lookup, lookupsById);
			lookupsById.put(lookupWithParent.getId(), lookupWithParent);
		}

		return from(lookupsById.values()) //
				.filter(withType(type));
	}

	private Lookup buildLookupWithParentLookup(final Lookup lookup, final Map<Long, Lookup> lookupsById) {
		final Lookup lookupWithParent;
		final Lookup parent = lookupsById.get(lookup.parentId);
		if (parent != null) {
			final Long grandparentId = parent.parentId;
			final Lookup parentWithGrandparent;
			if (grandparentId != null) {
				parentWithGrandparent = buildLookupWithParentLookup(parent, lookupsById);
			} else {
				parentWithGrandparent = parent;
			}
			lookupWithParent = Lookup.newInstance() //
					.clone(lookup) //
					.withParent(parentWithGrandparent) //
					.build();
		} else {
			lookupWithParent = lookup;
		}
		return lookupWithParent;
	}

	public static Predicate<Lookup> withType(final LookupType type) {
		logger.debug("filtering lookups with type '{}'", type);
		return new Predicate<Lookup>() {

			@Override
			public boolean apply(final Lookup input) {
				return input.type.equals(type);
			}

		};
	}

}
