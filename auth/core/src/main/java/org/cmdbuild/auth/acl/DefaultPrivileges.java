package org.cmdbuild.auth.acl;

public class DefaultPrivileges {

	/**
	 * SimplePrivilege does not imply any other privilege
	 */
	public static class SimplePrivilege implements CMPrivilege {

		@Override
		public boolean implies(final CMPrivilege privilege) {
			return privilege == this;
		}
	};

	/**
	 * Read access privilege
	 */
	public static final CMPrivilege READ = new SimplePrivilege();

	/**
	 * Write access implies read access
	 */
	public static final CMPrivilege WRITE = new SimplePrivilege() {

		@Override
		public boolean implies(final CMPrivilege privilege) {
			return super.implies(privilege) || privilege == READ;
		}
	};

	/**
	 * Write access implies read access
	 */
	public static final CMPrivilege NONE = new SimplePrivilege() {

		@Override
		public boolean implies(final CMPrivilege privilege) {
			return false;
		}
	};

	/**
	 * God privilege is used because people belonging to an administration group
	 * are granted full privileges.
	 */
	public static final CMPrivilege GOD = new CMPrivilege() {

		@Override
		public boolean implies(final CMPrivilege privilege) {
			return true;
		}
	};

	/**
	 * Database Designers can change the DB schema.
	 */
	public static final CMPrivilege DATABASE_DESIGNER = new SimplePrivilege();

	/**
	 * Administrators are those users that can change the system configuration,
	 * manage users, groups, their menus and ACLs.
	 */
	public static final CMPrivilege ADMINISTRATOR = new SimplePrivilege();

	public static final String GLOBAL_PRIVILEGE_ID = null;
}
