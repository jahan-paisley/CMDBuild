package org.cmdbuild.cql.compiler;

import java.util.List;

import org.cmdbuild.cql.CQLBuilderListener.OrderByType;
import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.cql.compiler.from.FromElement;

public interface OrderBy extends CQLElement {

	public class OrderByElement {
		FromElement from;
		String attributeName;
		OrderByType type;

		public OrderByElement(final FromElement from, final String name, final OrderByType type) {
			this.from = from;
			this.attributeName = name;
			this.type = type;
		}

		public String getAttributeName() {
			return attributeName;
		}

		public FromElement getFrom() {
			return from;
		}

		public OrderByType getType() {
			return type;
		}
	}

	void add(ClassDeclaration classDecl, String name, OrderByType type);

	void add(DomainDeclaration domainDecl, String name, OrderByType type);

	List<OrderByElement> getElements();
}
