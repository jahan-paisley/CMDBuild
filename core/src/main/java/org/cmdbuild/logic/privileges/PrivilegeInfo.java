package org.cmdbuild.logic.privileges;

import org.cmdbuild.auth.acl.SerializablePrivilege;
import org.cmdbuild.auth.privileges.constants.PrivilegeMode;

public class PrivilegeInfo {

	private final String[] EMPTY_ATTRIBUTES_PRIVILEGES = new String[0];

	private final Long groupId;
	private final PrivilegeMode mode;
	private final SerializablePrivilege privilegedObject;

	private String privilegeFilter;
	private String[] attributesPrivileges;

	public PrivilegeInfo(final Long groupId, final SerializablePrivilege privilegedObject, final PrivilegeMode mode) {
		this.groupId = groupId;
		this.mode = mode;
		this.privilegedObject = privilegedObject;
	}

	public String getPrivilegeFilter() {
		return privilegeFilter;
	}

	public void setPrivilegeFilter(final String privilegeFilter) {
		this.privilegeFilter = privilegeFilter;
	}

	public String[] getAttributesPrivileges() {
		return (attributesPrivileges == null) ? EMPTY_ATTRIBUTES_PRIVILEGES : attributesPrivileges;
	}

	public void setAttributesPrivileges(final String[] attributesPrivileges) {
		this.attributesPrivileges = attributesPrivileges;
	}

	public PrivilegeMode getMode() {
		return mode;
	}

	public Long getPrivilegedObjectId() {
		return privilegedObject.getId();
	}

	public String getPrivilegedObjectName() {
		return privilegedObject.getName();
	}

	public String getPrivilegedObjectDescription() {
		return privilegedObject.getDescription();
	}

	public Long getGroupId() {
		return groupId;
	}

	public String getPrivilegeId() {
		return privilegedObject.getPrivilegeId();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((groupId == null) ? 0 : groupId.hashCode());
		result = prime * result + ((mode == null) ? 0 : mode.hashCode());
		result = prime * result + ((privilegedObject == null) ? 0 : privilegedObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof PrivilegeInfo)) {
			return false;
		}
		final PrivilegeInfo other = (PrivilegeInfo) obj;
		return mode.equals(other.mode) //
				&& groupId.equals(other.getGroupId()) //
				&& getPrivilegedObjectId().equals(other.getPrivilegedObjectId());
	}

}