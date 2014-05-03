package org.cmdbuild.auth.acl;

public interface CMPrivilege {

	// It will be needed for a generic privilege implementation
	// String getName();

	/**
	 * Returns if the privilege is implied by this. The one privilege must
	 * always imply a privilege of the same type.
	 * 
	 * @param privilege
	 * @return if the privilege is implied
	 */
	boolean implies(CMPrivilege privilege);
}
