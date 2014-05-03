package org.cmdbuild.services.soap.types;

public class Lookup {
	
	private int id;
	private String type;
	private String description;
	private int position;
	private int parentId;
	private String notes;
	private String code;
	private Lookup parent;
	
	public Lookup() { }
	
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
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Lookup getParent() {
		return parent;
	}	
	public void setParent(Lookup parent) {
		this.parent = parent;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
}
