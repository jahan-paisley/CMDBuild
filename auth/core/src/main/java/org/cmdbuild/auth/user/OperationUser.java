package org.cmdbuild.auth.user;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.acl.PrivilegeContext;

public class OperationUser {

	private final PrivilegeContext privilegeCtx;
	private final AuthenticatedUser authUser;
	private CMGroup selectedGroup;

	/**
	 * 
	 * @param authUser
	 * @param privilegeCtx
	 * @param selectedGroup
	 *            it could be the default group or the only group te user
	 *            belongs to or the selected group (from web UI)
	 */
	public OperationUser(final AuthenticatedUser authUser, final PrivilegeContext privilegeCtx,
			final CMGroup selectedGroup) {
		Validate.notNull(authUser);
		Validate.notNull(privilegeCtx);
		Validate.notNull(selectedGroup);
		this.privilegeCtx = privilegeCtx;
		this.authUser = authUser;
		this.selectedGroup = selectedGroup;
	}

	/**
	 * An authenticated user is valid if it has a preferred group selected. The
	 * preferred group is the group that the user chose at the login. If the
	 * user belongs to one group or if it belongs to multiple groups but it has
	 * a default group, the preferred group is already selected.
	 * 
	 * @return
	 */
	public boolean isValid() {
		return !(getPreferredGroup() instanceof NullGroup);
	}

	public AuthenticatedUser getAuthenticatedUser() {
		return authUser;
	}

	/**
	 * @param selectedGroup
	 *            is the group that the user selected at the login time (if
	 *            requested)
	 */
	public void selectGroup(final CMGroup selectedGroup) {
		this.selectedGroup = selectedGroup;
	}

	/**
	 * Returns the group with which the user logged in. It can be the default
	 * group or the only group which the user belongs to or the selected group
	 * 
	 * @return
	 */
	public CMGroup getPreferredGroup() {
		return selectedGroup;
	}

	/**
	 * Don't remove now, used from Spring.
	 * 
	 * @deprecated access to {@link PrivilegeContext} directly.
	 */
	@Deprecated
	public PrivilegeContext getPrivilegeContext() {
		return privilegeCtx;
	}

	/**
	 * Impersonates another user, if possible. This method should be called by
	 * the AuthenticationService.
	 * 
	 * @param user
	 *            user to impersonate
	 */
	public void impersonate(final CMUser user) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("The impersonate method is not implemented yet");
	}

	/**
	 * @deprecated access to {@link PrivilegeContext} directly.
	 */
	@Deprecated
	public boolean hasReadAccess(final CMPrivilegedObject privilegedObject) {
		return privilegeCtx.hasReadAccess(privilegedObject);
	}

	/**
	 * @deprecated access to {@link PrivilegeContext} directly.
	 */
	@Deprecated
	public boolean hasWriteAccess(final CMPrivilegedObject privilegedObject) {
		return privilegeCtx.hasWriteAccess(privilegedObject);
	}

	/**
	 * @deprecated access to {@link PrivilegeContext} directly.
	 */
	@Deprecated
	public boolean hasAdministratorPrivileges() {
		return privilegeCtx.hasAdministratorPrivileges();
	}

	/**
	 * @deprecated access to {@link PrivilegeContext} directly.
	 */
	@Deprecated
	public boolean hasDatabaseDesignerPrivileges() {
		return privilegeCtx.hasDatabaseDesignerPrivileges();
	}

	/**
	 * @deprecated access to {@link PrivilegeContext} directly.
	 */
	@Deprecated
	public boolean hasPrivilege(final CMPrivilege privilege) {
		return privilegeCtx.hasPrivilege(privilege);
	}

	/**
	 * @deprecated access to {@link PrivilegeContext} directly.
	 */
	@Deprecated
	public boolean hasPrivilege(final CMPrivilege requested, final CMPrivilegedObject privilegedObject) {
		return privilegeCtx.hasPrivilege(requested, privilegedObject);
	}

}
