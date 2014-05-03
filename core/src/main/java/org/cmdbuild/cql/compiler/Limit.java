package org.cmdbuild.cql.compiler;

import org.cmdbuild.cql.CQLBuilderListener.FieldInputValue;

public interface Limit extends CQLElement {

	void setLimit(int limit);

	void setLimit(FieldInputValue limit);

	Object getLimitValue();
}
