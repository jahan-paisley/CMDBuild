package org.cmdbuild.cql.compiler.from;

public interface ClassDeclaration extends FromElement {

	void setName(String className);

	void setId(int classId);

	void setAs(String classAs);

	String getName();

	int getId();

	String getAs();

	boolean isClass(String name); // check name && as
}
