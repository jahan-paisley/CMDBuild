package org.cmdbuild.auth;

import org.cmdbuild.services.auth.UserType;

public interface AuthenticationStore {

	UserType getType();

	void setType(UserType type);

	Login getLogin();

	void setLogin(Login login);

}
