package org.cmdbuild.cql.compiler.select;

import java.util.List;

import org.cmdbuild.cql.compiler.CQLElement;
import org.cmdbuild.cql.compiler.from.FromElement;

public interface SelectElement<T extends FromElement> extends CQLElement {
	void setDeclaration(T declaration);

	T getDeclaration();

	void add(SelectItem item);

	List<SelectItem> getElements();
}
