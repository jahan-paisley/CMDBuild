package org.cmdbuild.auth.acl;

public class PrivilegePair {

	public static final SerializablePrivilege GLOBAL_PRIVILEGE = new SerializablePrivilege() {

		@Override
		public String getPrivilegeId() {
			return DefaultPrivileges.GLOBAL_PRIVILEGE_ID;
		}

		@Override
		public String getName() {
			return "";
		}

		@Override
		public String getDescription() {
			return "";
		}

		@Override
		public Long getId() {
			return new Long(0);
		}
	};

	public final String name;
	public SerializablePrivilege privilegedObject;
	public final CMPrivilege privilege;
	public String privilegeFilter;
	public String[] attributesPrivileges = new String[0];
	public String privilegedObjectType;

	public PrivilegePair(final CMPrivilege privilege) {
		this.name = GLOBAL_PRIVILEGE.getPrivilegeId();
		this.privilegedObject = GLOBAL_PRIVILEGE;
		this.privilege = privilege;
	}

	public PrivilegePair(final SerializablePrivilege privilegedObject, final String privilegedObjectType,
			final CMPrivilege privilege) {
		this.name = privilegedObject.getPrivilegeId();
		this.privilegedObject = privilegedObject;
		this.privilege = privilege;
		this.privilegedObjectType = privilegedObjectType;
	}

	/**
	 * @deprecated Must be used only by tests
	 */
	@Deprecated
	public PrivilegePair(final String name, final CMPrivilege privilege) {
		this.name = name;
		this.privilege = privilege;
	}
}
