package org.cmdbuild.workflow.type;

import java.io.Serializable;

import org.cmdbuild.common.annotations.Legacy;

@Legacy("Kept for backward compatibility")
public class LookupType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	int id;
	String type;
	String description;
	String code;
	
	public LookupType() {
		id = -1;
		type = "";
		description = "";
		code="";
	}
	public LookupType(int id, String type, String description, String code) {
		super();
		this.id = id;
		this.type = type;
		this.description = description;
		this.code=code;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	public boolean checkValidity() {
		return id > 0;
	}
	
	@Override
	public String toString(){
			return "LookupType[id: "+this.id+" code: "+this.code+" description: "+this.description+"]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + id;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		LookupType other = (LookupType) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}
