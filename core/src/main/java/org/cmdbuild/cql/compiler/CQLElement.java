package org.cmdbuild.cql.compiler;

import org.cmdbuild.cql.compiler.factory.AbstractElementFactory;

/**
 * Represent a generic element of a CQL expression (and by definition a CQL
 * expression itself)
 */
public interface CQLElement {
	void setElementFactory(AbstractElementFactory factory);

	CQLElement parent();

	<T extends CQLElement> T parentAs();

	void setParent(CQLElement element);
}
