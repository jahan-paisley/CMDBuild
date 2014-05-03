package org.cmdbuild.auth.acl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class NullGroup implements CMGroup {

	private Long nonExistentGroupId;

	public NullGroup() {
	}

	public NullGroup(final Long groupId) {
		this.nonExistentGroupId = groupId;
	}

	@Override
	public Long getId() {
		throw new NoSuchElementException("The group with id " + nonExistentGroupId + " is not present in the database");
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
	public List<PrivilegePair> getAllPrivileges() {
		return Lists.newArrayList();
	}

	@Override
	public Set<String> getDisabledModules() {
		return Sets.newHashSet();
	}

	@Override
	public Long getStartingClassId() {
		return null;
	}

	@Override
	public String getEmail() {
		return "";
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public boolean isRestrictedAdmin() {
		return false;
	}

	@Override
	public boolean isActive() {
		return false;
	}

	@Override
	public String toString() {
		return String.format("[Group %s]", this.getName());
	}
}
