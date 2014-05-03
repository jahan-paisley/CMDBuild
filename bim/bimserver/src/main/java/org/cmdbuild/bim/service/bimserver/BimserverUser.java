package org.cmdbuild.bim.service.bimserver;

import org.bimserver.interfaces.objects.SUser;
import org.cmdbuild.bim.service.BimError;
import org.cmdbuild.bim.service.BimUser;

public class BimserverUser implements BimUser {

	private final SUser user;

	protected BimserverUser(final SUser user) {
		this.user = user;
	};

	@Override
	public String getIdentifier() {
		final Long uid = new Long(user.getOid());
		return uid.toString();
	}

	@Override
	public String getPassword() {
		final Exception e = new Exception();
		throw new BimError("Method not implemented", e);
	}

	@Override
	public String getName() {
		return user.getName();
	}

}
