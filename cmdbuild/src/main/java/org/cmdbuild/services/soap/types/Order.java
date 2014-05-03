package org.cmdbuild.services.soap.types;

public class Order {
	
	private String columnName;
	private String type;
	
	public Order(){ }
	
	public String getColumnName() {
		return columnName;
	}
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		if (type.equals("ASC") || type.equals("DESC")){
			this.type = type;
		}
	}

}
