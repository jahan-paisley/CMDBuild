package org.cmdbuild.auth.privileges.constants;

/**
 * An enumeration that lists all privileges type. One privilege can be referred
 * to different type of privileged object.
 */
public enum PrivilegedObjectType {

	CLASS("Class"), //
	VIEW("View"), //
	FILTER("Filter");

	private String value;

	PrivilegedObjectType(final String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
