package org.cmdbuild.services.soap.structure;

import org.cmdbuild.services.soap.types.Metadata;

public class MenuSchema {

	private String menuType;
	private String description;
	private String classname;
	private int id;
	private int parentId;
	private int position;
	private String privilege;
	private Boolean defaultToDisplay;
	private Metadata[] metadata;
	private MenuSchema[] children;

	public MenuSchema() {
	}

	public Boolean getDefaultToDisplay() {
		return defaultToDisplay;
	}

	public void setDefaultToDisplay(Boolean defaultToDisplay) {
		this.defaultToDisplay = defaultToDisplay;
	}

	public boolean isLeaf() {
		return (children == null) || (children.length == 0);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getMenuType() {
		return menuType;
	}

	public void setMenuType(String menuType) {
		this.menuType = menuType;
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

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public MenuSchema[] getChildren() {
		return children;
	}

	public void setChildren(MenuSchema[] children) {
		this.children = children;
	}

	public String getPrivilege() {
		return privilege;
	}

	public void setPrivilege(String privilege) {
		this.privilege = privilege;
	}

	public Metadata[] getMetadata() {
		return metadata;
	}

	public void setMetadata(Metadata[] metadata) {
		this.metadata = metadata;
	}

}
