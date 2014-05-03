package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.cql.compiler.GroupBy;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;

public class GroupByImpl extends CQLElementImpl implements GroupBy {

	List<GroupByElement> elements = new ArrayList<GroupByElement>();

	@Override
	public void add(final ClassDeclaration classDecl, final String attributeName) {
		elements.add(new GroupByElement(classDecl, attributeName));
	}

	@Override
	public void add(final DomainDeclaration domainDecl, final String attributeName) {
		elements.add(new GroupByElement(domainDecl, attributeName));
	}

	@Override
	public List<GroupByElement> getElements() {
		return elements;
	}

}
