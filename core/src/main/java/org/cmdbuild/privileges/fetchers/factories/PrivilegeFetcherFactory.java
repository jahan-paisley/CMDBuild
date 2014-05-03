package org.cmdbuild.privileges.fetchers.factories;

import org.cmdbuild.privileges.fetchers.PrivilegeFetcher;

public interface PrivilegeFetcherFactory {

	/**
	 * 
	 * @return the PrivilegeFetcher for a specific CMPrivilegedObject. Note that
	 *         each CMPrivilegedObject has a custom PrivilegeFetcher
	 *         implementation
	 */
	PrivilegeFetcher create();

	/**
	 * Must be set before invoking create method
	 * 
	 * @param groupId
	 *            is the id of the group for which the PrivilegeFetcher will
	 *            fetch the privileges
	 */
	void setGroupId(Long groupId);

}
