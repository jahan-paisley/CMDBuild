package org.cmdbuild.auth.acl;

public interface PrivilegeContextFactory {

	/**
	 * Factory method
	 * 
	 * @param authUser
	 * @return an object containing all privileges for the user that logged in
	 *         with a specific group
	 */
	public PrivilegeContext buildPrivilegeContext(CMGroup... groups);

}
