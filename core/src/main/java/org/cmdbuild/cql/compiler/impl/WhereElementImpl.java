package org.cmdbuild.cql.compiler.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cmdbuild.cql.CQLBuilderListener.WhereType;
import org.cmdbuild.cql.compiler.from.FromElement;
import org.cmdbuild.cql.compiler.where.WhereElement;

public class WhereElementImpl extends CQLElementImpl implements WhereElement {

	boolean isNot = false;
	WhereType type;
	FromElement scope;
	List<WhereElement> elements = new ArrayList<WhereElement>();

	@Override
	public void add(final WhereElement element) {
		elements.add(element);
	}

	@Override
	public Collection<WhereElement> getElements() {
		return elements;
	}

	@Override
	public FromElement getScope() {
		return scope;
	}

	@Override
	public WhereType getType() {
		return type;
	}

	@Override
	public boolean isNot() {
		return isNot;
	}

	@Override
	public void setIsNot(final boolean isNot) {
		this.isNot = isNot;
	}

	@Override
	public void setScope(final FromElement classOrDomain) {
		this.scope = classOrDomain;
	}

	@Override
	public void setType(final WhereType type) {
		this.type = type;
	}

}
