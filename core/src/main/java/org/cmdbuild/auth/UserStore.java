package org.cmdbuild.auth;

import org.cmdbuild.auth.user.OperationUser;

public interface UserStore {

	/**
	 * Returns the operation user from the session. It must always be a valid
	 * (not null) user: "Use Null Objects, Luke!"
	 * 
	 * @return the operation user for this request
	 */
	OperationUser getUser();

	/**
	 * Sets the operation user in this session.
	 * 
	 * @param user
	 */
	void setUser(OperationUser user);

}
