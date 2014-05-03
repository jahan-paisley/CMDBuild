package org.cmdbuild.services.cache.wrappers;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newConcurrentMap;

import java.util.List;
import java.util.Map;

import org.cmdbuild.data.store.ForwardingStore;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.services.cache.CachingService.Cacheable;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class CachingStore<T extends Storable> extends ForwardingStore<T> implements Cacheable {

	private static final Marker marker = MarkerFactory.getMarker(CachingStore.class.getName());

	private static class Cache<T extends Storable> {

		private final Map<String, T> cache = newConcurrentMap();
		private final Store<T> store;

		public Cache(final Store<T> store) {
			this.store = store;
		}

		private void initCacheIfEmpty() {
			if (cache.isEmpty()) {
				Cacheable.logger.info(marker, "initializing cache");
				for (final T storable : store.list()) {
					_add(storable);
				}
			}
		}

		public T get(final Storable storable) {
			initCacheIfEmpty();
			return cache.get(storable.getIdentifier());
		}

		public void add(final T storable) {
			initCacheIfEmpty();
			_add(storable);
		}

		private void _add(final T storable) {
			cache.put(storable.getIdentifier(), storable);
		}

		public void remove(final Storable storable) {
			initCacheIfEmpty();
			cache.remove(storable.getIdentifier());
		}

		public List<T> values() {
			initCacheIfEmpty();
			return newArrayList(cache.values());
		}

		public void clear() {
			Cacheable.logger.info(marker, "clearing cache for '{}'", store.getClass());
			cache.clear();
		}

	}

	private final Cache<T> cache;

	public CachingStore(final Store<T> store) {
		super(store);
		this.cache = new Cache<T>(store);
	}

	@Override
	public Storable create(final T storable) {
		final Storable created = super.create(storable);
		final T readed = super.read(created);
		cache.add(readed);
		return created;
	}

	@Override
	public T read(final Storable storable) {
		return cache.get(storable);
	}

	@Override
	public void update(final T storable) {
		super.update(storable);
		cache.add(storable);
	}

	@Override
	public void delete(final Storable storable) {
		super.delete(storable);
		cache.remove(storable);
	}

	@Override
	public List<T> list() {
		return cache.values();
	}

	@Override
	public void clearCache() {
		cache.clear();
	}

}
