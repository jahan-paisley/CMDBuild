package org.cmdbuild.auth.acl;

public interface SerializablePrivilege extends CMPrivilegedObject {

	Long getId();

	String getName();

	String getDescription();
}
