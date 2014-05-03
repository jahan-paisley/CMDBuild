package org.cmdbuild.auth;

import java.util.Map;

import org.cmdbuild.auth.acl.CMGroup;

public interface GroupFetcher {

	/**
	 * Retrieves all groups stored in the database
	 * 
	 * @return
	 */
	Iterable<CMGroup> fetchAllGroups();

	/**
	 * Retrieves a map of all groups stored in the database.
	 * 
	 * @return a map where the key is the id of the group and the value is a
	 *         group object
	 */
	Map<Long, CMGroup> fetchAllGroupsMap();

	/**
	 * Retrieves the group with the specified id.
	 * 
	 * @param groupId
	 * @return the CMGroup if it exists, a NullGroup otherwise
	 */
	CMGroup fetchGroupWithId(Long groupId);

	/**
	 * Retrieves the group with the specified name.
	 * 
	 * @param groupName
	 * @return the CMGroup if it exists, a NullGroup otherwise
	 */
	CMGroup fetchGroupWithName(String groupName);

	/**
	 * Changes the status to the group with the specified id
	 * 
	 * @param groupId
	 * @param isActive
	 * @return
	 */
	CMGroup changeGroupStatusTo(Long groupId, boolean isActive);

}
