package org.cmdbuild.auth;

import org.cmdbuild.common.digest.Base64Digester;
import org.cmdbuild.dao.view.CMDataView;

/**
 * Checks password stored in the DAO layer
 */
public class LegacyDBAuthenticator extends DatabaseAuthenticator {

	public LegacyDBAuthenticator(final CMDataView view) {
		super(view);
	}

	public LegacyDBAuthenticator(final CMDataView view, final Base64Digester digester) {
		super(view, digester);
	}

	@Override
	protected String loginAttributeName(final Login login) {
		return userNameAttribute();
	}

}
