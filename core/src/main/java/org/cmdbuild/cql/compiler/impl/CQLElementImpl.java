package org.cmdbuild.cql.compiler.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.cmdbuild.cql.compiler.CQLElement;
import org.cmdbuild.cql.compiler.factory.AbstractElementFactory;

@SuppressWarnings("unchecked")
public class CQLElementImpl implements CQLElement {
	AbstractElementFactory factory;
	CQLElement parent;
	Set<CQLElementImpl> children = new HashSet<CQLElementImpl>();

	@Override
	public CQLElement parent() {
		return parent;
	}

	@Override
	public <T extends CQLElement> T parentAs() {
		return (T) parent;
	}

	@Override
	public void setElementFactory(final AbstractElementFactory factory) {
		this.factory = factory;
	}

	@Override
	public void setParent(final CQLElement element) {
		if (element == null) {
			return;
		}
		if (this.parent == null) {
			this.parent = element;
			((CQLElementImpl) element).children.add(this);
		} else {
			System.out.println("Cannot re-set the parent of a CQL element - " + this.getClass().getCanonicalName()
					+ ", " + element.getClass().getCanonicalName() + " - current parent: "
					+ parent.getClass().getCanonicalName());
		}
	}

	public Collection<CQLElementImpl> getChildren() {
		return children;
	}

}
