package org.cmdbuild.services.auth;

import java.io.Serializable;

public class UserGroup implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String description;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	@Override
	public boolean equals(final Object object) {
		if (this == object) {
			return true;
		}

		if ((object == null) || (this.getClass() != object.getClass())) {
			return false;
		}

		final UserGroup userGroup = UserGroup.class.cast(object);

		return name.equals(userGroup.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

}
