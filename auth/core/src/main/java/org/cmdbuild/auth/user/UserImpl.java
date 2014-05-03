package org.cmdbuild.auth.user;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.common.Builder;

public class UserImpl implements CMUser {

	public static class UserImplBuilder implements Builder<UserImpl> {

		private Long id;
		private String username;
		private String description;
		private String email;
		private boolean active = true;
		private final Set<String> groupNames;
		private final List<String> groupDescriptions;
		private String defaultGroupName;

		private UserImplBuilder() {
			this.groupNames = new HashSet<String>();
			this.groupDescriptions = new LinkedList<String>();
		}

		public UserImplBuilder withId(final Long id) {
			this.id = id;
			return this;
		}

		public UserImplBuilder withUsername(final String username) {
			this.username = username;
			return this;
		}

		public UserImplBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public UserImplBuilder withGroupName(final String groupName) {
			this.groupNames.add(groupName);
			return this;
		}

		public UserImplBuilder withGroupDescription(final String groupName) {
			this.groupDescriptions.add(groupName);
			return this;
		}

		public UserImplBuilder withGroupNames(final Set<String> groupNames) {
			this.groupNames.addAll(groupNames);
			return this;
		}

		public UserImplBuilder withEmail(final String email) {
			this.email = email;
			return this;
		}

		public UserImplBuilder withActiveStatus(final boolean active) {
			this.active = active;
			return this;
		}

		public UserImplBuilder withDefaultGroupName(final String defaultGroupName) {
			this.defaultGroupName = defaultGroupName;
			return this;
		}

		@Override
		public UserImpl build() {
			Validate.notNull(username);
			Validate.notNull(description);
			Validate.noNullElements(groupNames);

			java.util.Collections.sort(groupDescriptions);

			return new UserImpl(this);
		}
	}

	private final Long id;
	private final String username;
	private final String description;
	private final String email;
	private final boolean active;
	private final Set<String> groupNames;
	private final List<String> groupDescriptions;
	private final String defaultGroupName;

	private UserImpl(final UserImplBuilder builder) {
		this.id = builder.id;
		this.username = builder.username;
		this.description = builder.description;
		this.email = builder.email;
		this.active = builder.active;
		this.groupNames = builder.groupNames;
		this.defaultGroupName = builder.defaultGroupName;
		this.groupDescriptions = builder.groupDescriptions;
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public Set<String> getGroupNames() {
		return this.groupNames;
	}

	@Override
	public List<String> getGroupDescriptions() {
		return this.groupDescriptions;
	}

	@Override
	public String getDefaultGroupName() {
		return defaultGroupName;
	}

	@Override
	public String getEmail() {
		return email;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public static UserImplBuilder newInstanceBuilder() {
		return new UserImplBuilder();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (!CMUser.class.isAssignableFrom(obj.getClass())) {
			return false;
		}
		final CMUser other = CMUser.class.cast(obj);
		return username.equals(other.getUsername());
	}

	@Override
	public int hashCode() {
		return username.hashCode();
	}

}
