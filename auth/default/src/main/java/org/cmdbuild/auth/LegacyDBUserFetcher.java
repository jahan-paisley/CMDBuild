package org.cmdbuild.auth;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;

/**
 * Fetches the users in the legacy database tables decided by the DBA
 */
public class LegacyDBUserFetcher extends DBUserFetcher {

	public LegacyDBUserFetcher(final CMDataView view) {
		super(view);
	}

	@Override
	protected final CMClass userClass() {
		return view.findClass("User");
	}

	@Override
	protected final CMClass roleClass() {
		return view.findClass("Role");
	}

	@Override
	protected final String userEmailAttribute() {
		return "Email";
	}

	@Override
	protected final String userNameAttribute() {
		return "Username";
	}

	@Override
	protected final String userDescriptionAttribute() {
		return "Description";
	}

	@Override
	protected final String userPasswordAttribute() {
		return "Password";
	}

	@Override
	protected final String userIdAttribute() {
		return "Id";
	}

	@Override
	protected CMDomain userGroupDomain() {
		return view.findDomain("UserRole");
	}

}
