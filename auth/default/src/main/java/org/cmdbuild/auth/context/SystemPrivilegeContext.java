package org.cmdbuild.auth.context;

import java.util.List;

import org.cmdbuild.auth.acl.CMPrivilege;
import org.cmdbuild.auth.acl.CMPrivilegedObject;
import org.cmdbuild.auth.acl.PrivilegeContext;

import com.google.common.collect.Lists;

public class SystemPrivilegeContext implements PrivilegeContext {

	@Override
	public boolean hasPrivilege(CMPrivilege privilege) {
		return true;
	}

	@Override
	public boolean hasAdministratorPrivileges() {
		return true;
	}

	@Override
	public boolean hasDatabaseDesignerPrivileges() {
		return true;
	}

	@Override
	public boolean hasPrivilege(CMPrivilege requested, CMPrivilegedObject privilegedObject) {
		return true;
	}

	@Override
	public boolean hasReadAccess(CMPrivilegedObject privilegedObject) {
		return true;
	}

	@Override
	public boolean hasWriteAccess(CMPrivilegedObject privilegedObject) {
		return true;
	}

	@Override
	public PrivilegedObjectMetadata getMetadata(CMPrivilegedObject privilegedObject) {
		return new PrivilegedObjectMetadata() {
			
			@Override
			public List<String> getFilters() {
				return Lists.newArrayList();
			}
			
			@Override
			public List<String> getAttributesPrivileges() {
				return Lists.newArrayList();
			}
		};
	}

}
