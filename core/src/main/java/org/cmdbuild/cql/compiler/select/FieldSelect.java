package org.cmdbuild.cql.compiler.select;

public interface FieldSelect extends SelectItem {

	void setName(String attributeName);

	void setAs(String attributeAs);

	String getName();

	String getAs();
}
