package org.cmdbuild.privileges.fetchers;

import org.cmdbuild.auth.acl.PrivilegePair;

/**
 * A PrivilegeFetcher must fetch privileges for
 */
public interface PrivilegeFetcher {

	/**
	 * Fetches the privileges
	 * 
	 * @return
	 */
	Iterable<PrivilegePair> fetch();

}
