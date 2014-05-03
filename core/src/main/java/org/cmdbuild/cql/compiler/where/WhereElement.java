package org.cmdbuild.cql.compiler.where;

import java.util.Collection;

import org.cmdbuild.cql.CQLBuilderListener.WhereType;
import org.cmdbuild.cql.compiler.CQLElement;
import org.cmdbuild.cql.compiler.from.FromElement;

public interface WhereElement extends CQLElement {
	void setType(WhereType type);

	void setIsNot(boolean isNot);

	void add(WhereElement element);

	void setScope(FromElement classOrDomain);

	WhereType getType();

	boolean isNot();

	Collection<WhereElement> getElements();

	FromElement getScope();
}
