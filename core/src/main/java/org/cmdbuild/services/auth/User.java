package org.cmdbuild.services.auth;

public interface User {

	int getId();

	String getName();

	String getDescription();

	String getEncryptedPassword();
}
