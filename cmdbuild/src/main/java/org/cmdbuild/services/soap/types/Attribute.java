package org.cmdbuild.services.soap.types;

public class Attribute {

	private String name;
	private String value;
	private String code;

	public Attribute(){ }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}

	public String toString(){
		return "["+name+" -> "+"value: "+value+" code:"+code+" ]";
	}
}
