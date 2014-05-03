package org.cmdbuild.logic.data.access.lock;

import org.cmdbuild.exception.ConsistencyException;

public interface LockCardManager {

	interface LockCardConfiguration {

		boolean isLockerUsernameVisible();

		long getExpirationTimeInMilliseconds();

	}

	/**
	 * Lock the editing of the card
	 * with the given cardId, and throws
	 * a ConsistencyException if the card
	 * is already locked by another user
	 * 
	 * @param cardId
	 */
	void lock(Long cardId);

	void unlock(Long cardId);

	void unlockAll();

	void checkLocked(Long cardId);

	void checkLockerUser(Long cardId, String userName) throws ConsistencyException;

	void updateLockCardConfiguration(LockCardConfiguration configuration);
}
