package org.cmdbuild.auth.privileges.constants;

public enum PrivilegeMode {

	READ("r"), //
	WRITE("w"), //
	NONE("-");

	private String value;

	PrivilegeMode(final String value) {
		this.value = value;
	}

	public String getValue() {
		return this.value;
	}

}
