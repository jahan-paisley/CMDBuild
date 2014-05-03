package org.cmdbuild.services.store;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import org.cmdbuild.data.store.Groupable;
import org.cmdbuild.data.store.Storable;
import org.cmdbuild.data.store.Store;
import org.cmdbuild.logic.data.access.lock.LockCardManager;
import org.cmdbuild.model.LockedCard;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;

public class LockedCardStore implements Store<LockedCard> {

	private Cache<String, LockedCard> lockedCards;

	/**
	 * 
	 * @param expirationTimeInMilliseconds
	 *            after this period of time the store is cleared
	 */
	public LockedCardStore(final LockCardManager.LockCardConfiguration configuration) {
		lockedCards = CacheBuilder.newBuilder() //
				.expireAfterWrite(configuration.getExpirationTimeInMilliseconds(), TimeUnit.MILLISECONDS) //
				.build();
	}

	public void setExpirationTime(final long expirationTimeInMilliseconds) {
		final Cache<String, LockedCard> updatedLockedCards = CacheBuilder.newBuilder() //
				.expireAfterWrite(expirationTimeInMilliseconds, TimeUnit.MILLISECONDS) //
				.build();
		for (final Entry<String, LockedCard> entry : lockedCards.asMap().entrySet()) {
			updatedLockedCards.put(entry.getKey(), entry.getValue());
		}
		lockedCards = updatedLockedCards;
	}

	@Override
	public Storable create(final LockedCard lockedCard) {
		lockedCard.setLockTimestamp(new Date());
		lockedCards.put(lockedCard.getIdentifier(), lockedCard);
		return lockedCard;
	}

	@Override
	public LockedCard read(final Storable storable) {
		return lockedCards.getIfPresent(storable.getIdentifier());
	}

	@Override
	public void update(final LockedCard lockedCard) {
		throw new UnsupportedOperationException("A locked card can't be modified");
	}

	@Override
	public void delete(final Storable storable) {
		lockedCards.invalidate(storable.getIdentifier());
	}

	@Override
	public List<LockedCard> list() {
		final Map<String, LockedCard> lockedCardsMap = lockedCards.asMap();
		return Lists.newArrayList(lockedCardsMap.values());
	}

	@Override
	public List<LockedCard> list(final Groupable groupable) {
		throw new UnsupportedOperationException();
	}

}
