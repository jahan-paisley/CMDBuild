package org.cmdbuild.cql.compiler.select;

@SuppressWarnings("unchecked")
public interface FunctionSelect extends SelectElement {

	void setName(String functionName);

	void setAs(String functionAs);

	String getName();

	String getAs();
}
