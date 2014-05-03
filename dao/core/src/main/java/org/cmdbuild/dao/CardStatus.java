package org.cmdbuild.dao;

public enum CardStatus {
	ACTIVE("A"), UPDATED("U"), INACTIVE("N"), INACTIVE_USER("D");

	private final String value;

	CardStatus(final String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	public static CardStatus fromString(final String value) {
		for (final CardStatus status : CardStatus.values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}
		return CardStatus.INACTIVE;
	}
}
