package org.cmdbuild.workflow.type;

import java.io.Serializable;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.annotations.Legacy;

@Legacy("Kept for backward compatibility")
public class ReferenceType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	int id;
	int idClass;
	String description;

	public ReferenceType() {
		id = -1;
		idClass = -1;
		description = "";
	}

	public ReferenceType(int id, int idClass, String description) {
		super();
		this.id = id;
		this.idClass = idClass;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getIdClass() {
		return idClass;
	}

	public void setIdClass(int idClass) {
		this.idClass = idClass;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean checkValidity() {
		return id > 0;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("idclass", idClass) //
				.append("id", id) //
				.append("description", description) //
				.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + idClass;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReferenceType other = (ReferenceType) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (idClass != other.idClass)
			return false;
		return true;
	}
}
