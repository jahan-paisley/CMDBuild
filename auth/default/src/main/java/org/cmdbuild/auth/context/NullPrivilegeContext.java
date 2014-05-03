package org.cmdbuild.auth.context;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.PrivilegeContext;

/**
 * Implementation of null object pattern. The user with this PrivilegeContext
 * could not perform any operation because it does not have any privilege
 */
public class NullPrivilegeContext implements PrivilegeContext {

	@Override
	public boolean hasPrivilege(final CMPrivilege privilege) {
		return false;
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		return false;
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		return false;
	}

	@Override
	public boolean hasPrivilege(final CMPrivilege requested, final CMPrivilegedObject privilegedObject) {
		return false;
	}

	@Override
	public boolean hasReadAccess(final CMPrivilegedObject privilegedObject) {
		return false;
	}

	@Override
	public boolean hasWriteAccess(final CMPrivilegedObject privilegedObject) {
		return false;
	}

	@Override
	public PrivilegedObjectMetadata getMetadata(final CMPrivilegedObject privilegedObject) {
		return null;
	}

}
