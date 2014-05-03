package org.cmdbuild.cql.compiler;

import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;

public interface From extends CQLElement {

	void setHistory(boolean history);

	void add(ClassDeclaration classDecl);

	boolean isHistory();

	<T extends ClassDeclaration> T searchClass(String nameOrRef);

	<T extends DomainDeclaration> T searchDomain(String nameOrRef);

	<T extends ClassDeclaration> T mainClass();
}
