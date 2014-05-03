package org.cmdbuild.services.soap.types;

public class CQLParameter {
	
	private String key;
	private String value;
	
	public CQLParameter() { }

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
