package org.cmdbuild.services.soap.connector;

public enum DomainDirection {
	DIRECT("directed"), INVERSE("inverted");

	private final String direction;

	DomainDirection(final String direction) {
		this.direction = direction;
	}

	public String getDirection() {
		return this.direction;
	}

	public static DomainDirection getDirection(final String action) {

		if (DomainDirection.DIRECT.getDirection().equals(action)) {
			return DomainDirection.DIRECT;
		} else {
			return DomainDirection.INVERSE;
		}
	}
}
