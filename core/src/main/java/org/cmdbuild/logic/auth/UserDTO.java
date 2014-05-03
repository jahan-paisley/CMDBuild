package org.cmdbuild.logic.auth;

import org.cmdbuild.common.Builder;

public class UserDTO {

	public static class UserDTOBuilder implements Builder<UserDTO> {

		private Long userId;
		private String description;
		private String username;
		private String password;
		private String email;
		private boolean active = true;
		private Long defaultGroupId;

		public UserDTOBuilder withUserId(final Long userId) {
			this.userId = userId;
			return this;
		}

		public UserDTOBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public UserDTOBuilder withUsername(final String username) {
			this.username = username;
			return this;
		}

		public UserDTOBuilder withPassword(final String password) {
			this.password = password;
			return this;
		}

		public UserDTOBuilder withEmail(final String email) {
			this.email = email;
			return this;
		}

		public UserDTOBuilder withActiveStatus(final boolean status) {
			this.active = status;
			return this;
		}

		public UserDTOBuilder withDefaultGroupId(final Long defaultGroupId) {
			this.defaultGroupId = defaultGroupId;
			return this;
		}

		@Override
		public UserDTO build() {
			return new UserDTO(this);
		}
	}

	public static class UserDTOCreationValidator implements ModelValidator<UserDTO> {
		@Override
		public boolean validate(final UserDTO userDTO) {
			if (userDTO.getUserId() != null || userDTO.getUsername() == null || userDTO.getPassword() == null) {
				return false;
			}
			return true;
		}
	}

	public static class UserDTOUpdateValidator implements ModelValidator<UserDTO> {
		@Override
		public boolean validate(final UserDTO userDTO) {
			if (userDTO.getUserId() == null) {
				return false;
			}
			return true;
		}
	}

	private final Long userId;
	private final String description;
	private final String username;
	private final String password;
	private final String email;
	private final boolean active;
	private final Long defaultGroupId;

	private UserDTO(final UserDTOBuilder builder) {
		this.userId = builder.userId;
		this.description = builder.description;
		this.username = builder.username;
		this.password = builder.password;
		this.email = builder.email;
		this.active = builder.active;
		this.defaultGroupId = builder.defaultGroupId;
	}

	public static UserDTOBuilder newInstance() {
		return new UserDTOBuilder();
	}

	public Long getUserId() {
		return userId;
	}

	public String getDescription() {
		return description;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public String getEmail() {
		return email;
	}

	public boolean isActive() {
		return active;
	}

	public Long getDefaultGroupId() {
		return defaultGroupId;
	}

}
