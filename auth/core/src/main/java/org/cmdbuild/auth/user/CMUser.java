package org.cmdbuild.auth.user;

import java.util.List;
import java.util.Set;

public interface CMUser {

	/**
	 * This identifier will be useful in the refactoring of the old messy code.
	 * It should be removed when it is not needed, and the name should always be
	 * used instead.
	 * 
	 * @return unique identifier
	 */
	Long getId();

	/**
	 * 
	 * @return unique human-readable identifier
	 */
	String getUsername();

	String getDescription();

	/**
	 * Returns a set of groups to which the user belongs
	 * 
	 * @return
	 */
	Set<String> getGroupNames();

	/**
	 * Returns a sorted list with the description of the user groups
	 * 
	 * @return
	 */
	List<String> getGroupDescriptions();

	/**
	 * Returns the name of the default group for this user, used to try and
	 * select the preferred group in the
	 * {@link org.cmdbuild.auth.AuthenticatedUser}
	 * 
	 * @return default group name or null if not set
	 */
	String getDefaultGroupName();

	/**
	 * 
	 * @return the email address of the user
	 */
	String getEmail();

	/**
	 * 
	 * @return true if the user is active, false otherwise
	 */
	boolean isActive();

	/**
	 * Two CMUsers are equal if their name is equal
	 * 
	 * @param obj
	 * @return if the two users are equal
	 */
	@Override
	boolean equals(final Object obj);
}
