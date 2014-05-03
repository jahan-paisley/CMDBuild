package org.cmdbuild.api.fluent;

public class LookupImpl extends CardDescriptor implements Lookup {

	private static final String className = "LookUp";

	private String type;
	private String code;
	private String description;

	public LookupImpl(final Integer id) {
		super(className, id);
	}

	@Override
	public String getType() {
		return type;
	}

	void setType(final String type) {
		this.type = type;
	}

	@Override
	public String getCode() {
		return code;
	}

	void setCode(final String code) {
		this.code = code;
	}

	@Override
	public String getDescription() {
		return description;
	}

	void setDescription(final String description) {
		this.description = description;
	}

}
