package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.cql.CQLBuilderListener.OrderByType;
import org.cmdbuild.cql.compiler.OrderBy;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;

public class OrderByImpl extends CQLElementImpl implements OrderBy {
	boolean isDefault = false;

	public void setDefault() {
		isDefault = true;
	}

	public boolean isDefault() {
		return isDefault;
	}

	List<OrderByElement> elements = new ArrayList<OrderByElement>();

	@Override
	public void add(final ClassDeclaration classDecl, final String name, final OrderByType type) {
		elements.add(new OrderByElement(classDecl, name, type));
	}

	@Override
	public void add(final DomainDeclaration domainDecl, final String name, final OrderByType type) {
		elements.add(new OrderByElement(domainDecl, name, type));
	}

	@Override
	public List<OrderByElement> getElements() {
		return elements;
	}

}
