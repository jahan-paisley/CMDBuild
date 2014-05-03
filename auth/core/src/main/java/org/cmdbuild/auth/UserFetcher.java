package org.cmdbuild.auth;

import java.util.List;

import org.cmdbuild.auth.user.CMUser;

public interface UserFetcher {

	CMUser fetchUser(Login login);

	List<CMUser> fetchUsersFromGroupId(Long groupId);

	List<Long> fetchUserIdsFromGroupId(Long groupId);

	CMUser fetchUserById(Long userId);

	List<CMUser> fetchAllUsers();

}
