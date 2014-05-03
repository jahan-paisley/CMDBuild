package org.cmdbuild.auth.acl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.Builder;

public class GroupImpl implements CMGroup {

	public static class GroupImplBuilder implements Builder<GroupImpl> {

		private Long id;
		private String name;
		private String description;
		private String email;
		private final List<PrivilegePair> privileges;
		private final Set<String> disabledModules;
		private Long startingClassId;
		private boolean active = true;
		private boolean isAdmin = false;
		public boolean isRestrictedAdmin = false;

		private GroupImplBuilder() {
			privileges = new ArrayList<PrivilegePair>();
			disabledModules = new HashSet<String>();
		}

		public GroupImplBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public GroupImplBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public GroupImplBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public GroupImplBuilder withEmail(final String email) {
			this.email = email;
			return this;
		}

		public GroupImplBuilder withPrivileges(final List<PrivilegePair> privileges) {
			this.privileges.addAll(privileges);
			return this;
		}

		public GroupImplBuilder withPrivilege(final PrivilegePair privilege) {
			this.privileges.add(privilege);
			return this;
		}

		public GroupImplBuilder withoutModule(final String moduleName) {
			this.disabledModules.add(moduleName);
			return this;
		}

		public GroupImplBuilder withStartingClassId(final Long classId) {
			this.startingClassId = classId;
			return this;
		}

		public GroupImplBuilder withActiveStatus(final boolean active) {
			this.active = active;
			return this;
		}

		public GroupImplBuilder administrator(final boolean isAdmin) {
			this.isAdmin = isAdmin;
			return this;
		}

		public GroupImplBuilder restrictedAdministrator(final boolean isRestrictedAdmin) {
			this.isRestrictedAdmin = isRestrictedAdmin;
			return this;
		}

		@Override
		public GroupImpl build() {
			Validate.notNull(name);
			if (description == null) {
				description = name;
			}
			return new GroupImpl(this);
		}
	}

	private final Long id;
	private final String name;
	private final String description;
	private final String email;
	private final List<PrivilegePair> privileges;
	private final Set<String> disabledModules;
	private final Long startingClassId;
	private final boolean active;
	private final boolean isAdmin;
	private final boolean isRestrictedAdmin;

	private GroupImpl(final GroupImplBuilder builder) {
		this.id = builder.id;
		this.name = builder.name;
		this.description = builder.description;
		this.email = builder.email;
		this.privileges = builder.privileges;
		this.disabledModules = builder.disabledModules;
		this.startingClassId = builder.startingClassId;
		this.active = builder.active;
		this.isAdmin = builder.isAdmin;
		this.isRestrictedAdmin = builder.isRestrictedAdmin;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public List<PrivilegePair> getAllPrivileges() {
		return privileges;
	}

	@Override
	public Set<String> getDisabledModules() {
		return this.disabledModules;
	}

	@Override
	public Long getStartingClassId() {
		return this.startingClassId;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public boolean isAdmin() {
		return isAdmin;
	}

	@Override
	public boolean isRestrictedAdmin() {
		return isRestrictedAdmin;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public static GroupImplBuilder newInstance() {
		return new GroupImplBuilder();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!CMGroup.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final CMGroup other = CMGroup.class.cast(obj);
		return name.equals(other.getName());
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return String.format("[Group %s]", this.getName());
	}

}
