package org.cmdbuild.model;

import java.util.Date;

import org.cmdbuild.data.store.Storable;

public class LockedCard implements Storable {

	private final Long cardId;
	private final String lockerUsername;
	private Date lockTimestamp;

	public LockedCard(final Long cardId, final String lockerUsername) {
		this.cardId = cardId;
		this.lockerUsername = lockerUsername;
	}

	@Override
	public String getIdentifier() {
		return cardId.toString();
	}

	public String getLockerUsername() {
		return lockerUsername;
	}

	public Date getLockTimestamp() {
		return lockTimestamp;
	}

	public void setLockTimestamp(final Date lockTimestamp) {
		this.lockTimestamp = lockTimestamp;
	}

	public long getTimeInSecondsSinceInsert() {
		final long currentTimestamp = new Date().getTime();
		final long differenceInMilliseconds = currentTimestamp - lockTimestamp.getTime();
		final long differenceInSeconds = differenceInMilliseconds / 1000;
		return differenceInSeconds;
	}

}
