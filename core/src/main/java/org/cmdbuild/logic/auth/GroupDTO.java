package org.cmdbuild.logic.auth;

import org.cmdbuild.common.Builder;

public class GroupDTO {

	public static class GroupDTOBuilder implements Builder<GroupDTO> {

		private Long groupId;
		private String name;
		private String description;
		private String email;
		private boolean active = true;
		private Boolean isAdministrator = false;
		private Boolean isRestrictedAdministrator = false;
		private Long startingClassId;

		public GroupDTOBuilder withGroupId(final Long groupId) {
			this.groupId = groupId;
			return this;
		}

		public GroupDTOBuilder withName(final String name) {
			this.name = name;
			return this;
		}

		public GroupDTOBuilder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public GroupDTOBuilder withEmail(final String email) {
			this.email = email;
			return this;
		}

		public GroupDTOBuilder withActiveStatus(final boolean status) {
			this.active = status;
			return this;
		}

		public GroupDTOBuilder withAdminFlag(final Boolean adminFlag) {
			this.isAdministrator = adminFlag;
			return this;
		}

		public GroupDTOBuilder withRestrictedAdminFlag(final Boolean restrictedAdmin) {
			this.isRestrictedAdministrator = restrictedAdmin;
			return this;
		}

		public GroupDTOBuilder withStartingClassId(final Long startingClassId) {
			this.startingClassId = startingClassId;
			return this;
		}

		@Override
		public GroupDTO build() {
			return new GroupDTO(this);
		}
	}

	public static class GroupDTOCreationValidator implements ModelValidator<GroupDTO> {
		@Override
		public boolean validate(final GroupDTO groupDTO) {
			if (groupDTO.getGroupId() != null || groupDTO.getName() == null) {
				return false;
			}
			return true;
		}
	}

	public static class GroupDTOUpdateValidator implements ModelValidator<GroupDTO> {
		@Override
		public boolean validate(final GroupDTO groupDTO) {
			if (groupDTO.getGroupId() == null) {
				return false;
			}
			return true;
		}
	}

	private final Long groupId;
	private final String name;
	private final String description;
	private final String email;
	private final boolean active;
	private final Boolean isAdministrator;
	private final Boolean isRestrictedAdministrator;
	private final Long startingClassId;

	private GroupDTO(final GroupDTOBuilder builder) {
		this.groupId = builder.groupId;
		this.name = builder.name;
		this.description = builder.description;
		this.email = builder.email;
		this.active = builder.active;
		this.isAdministrator = builder.isAdministrator;
		this.isRestrictedAdministrator = builder.isRestrictedAdministrator;
		this.startingClassId = builder.startingClassId;
	}

	public static GroupDTOBuilder newInstance() {
		return new GroupDTOBuilder();
	}

	public Long getGroupId() {
		return groupId;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public String getEmail() {
		return email;
	}

	public boolean isActive() {
		return active;
	}

	public Boolean isAdministrator() {
		return isAdministrator;
	}

	public Boolean isRestrictedAdministrator() {
		return isRestrictedAdministrator;
	}

	public Long getStartingClassId() {
		return startingClassId;
	}

}
