package org.cmdbuild.auth;

import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.entrytype.CMDomain;
import org.cmdbuild.dao.view.CMDataView;

/**
 * Fetches users from the application domain (for 3lv authentication)
 */
public class ApplicationDomainUserFetcher extends DBUserFetcher {

	public ApplicationDomainUserFetcher(final CMDataView view) {
		super(view);
	}

	@Override
	protected CMClass userClass() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected String userEmailAttribute() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected String userNameAttribute() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected final String userDescriptionAttribute() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected String userPasswordAttribute() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected String userIdAttribute() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected CMDomain userGroupDomain() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	protected CMClass roleClass() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
