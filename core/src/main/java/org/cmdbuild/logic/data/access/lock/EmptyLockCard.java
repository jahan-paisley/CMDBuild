package org.cmdbuild.logic.data.access.lock;

public class EmptyLockCard implements LockCardManager {

	@Override
	public void lock(final Long cardId) {
	}

	@Override
	public void unlock(final Long cardId) {
	}

	@Override
	public void unlockAll() {
	}

	@Override
	public void checkLockerUser(final Long cardId, final String userName) {
	}

	@Override
	public void checkLocked(final Long cardId) {
	}

	@Override
	public void updateLockCardConfiguration(final LockCardConfiguration configuration) {
	}

}
