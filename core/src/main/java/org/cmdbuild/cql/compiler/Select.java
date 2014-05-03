package org.cmdbuild.cql.compiler;

import java.util.Collection;

import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.cql.compiler.select.ClassSelect;
import org.cmdbuild.cql.compiler.select.DomainMetaSelect;
import org.cmdbuild.cql.compiler.select.DomainObjectsSelect;
import org.cmdbuild.cql.compiler.select.FunctionSelect;
import org.cmdbuild.cql.compiler.select.SelectElement;

public interface Select extends CQLElement {

	void setSelectAll();

	<T extends ClassSelect> T get(ClassDeclaration classDecl);

	<T extends ClassSelect> T getOrCreate(ClassDeclaration classDecl);

	<T extends DomainMetaSelect> T getMeta(DomainDeclaration domainDecl);

	<T extends DomainMetaSelect> T getMetaOrCreate(DomainDeclaration domainDecl);

	<T extends DomainObjectsSelect> T getObjects(DomainDeclaration domainDecl);

	<T extends DomainObjectsSelect> T getObjectsOrCreate(DomainDeclaration domainDecl);

	void add(ClassSelect classSelect);

	void add(DomainMetaSelect domMeta);

	void add(DomainObjectsSelect domObjs);

	void add(FunctionSelect fun);

	boolean isSelectAll();

	@SuppressWarnings("unchecked")
	Collection<SelectElement> getElements();
}
