package org.cmdbuild.auth;

import org.apache.commons.lang3.Validate;

public class Login {

	public enum LoginType {

		USERNAME, EMAIL;

		private static LoginType fromLoginString(final String loginString) {
			if (loginString.contains("@")) {
				return LoginType.EMAIL;
			} else {
				return LoginType.USERNAME;
			}
		}
	}

	private final String value;
	private final LoginType type;

	public static Login newInstance(final String loginString) {
		Validate.notNull(loginString, "Null login string");
		return new Login(loginString, LoginType.fromLoginString(loginString));
	}

	/*
	 * Basically used by the tests
	 */
	public static Login newInstance(final String loginString, final LoginType type) {
		Validate.notNull(loginString, "Null login string");
		Validate.notNull(type, "Null type");
		return new Login(loginString, type);
	}

	private Login(final String value, final LoginType type) {
		this.value = value;
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public LoginType getType() {
		return type;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (!getClass().isAssignableFrom(obj.getClass())) {
			return false;
		}
		final Login login = Login.class.cast(obj);
		return (getType() == login.getType() && getValue().equals(login.getValue()));
	}

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public String toString() {
		return String.format("%s/%s", value, type);
	}
}
