package org.cmdbuild.cql.compiler;

import java.util.List;

import org.cmdbuild.cql.compiler.from.ClassDeclaration;
import org.cmdbuild.cql.compiler.from.DomainDeclaration;
import org.cmdbuild.cql.compiler.from.FromElement;

public interface GroupBy extends CQLElement {
	public class GroupByElement {
		FromElement from;
		String attributeName;

		public GroupByElement(final FromElement from, final String name) {
			this.from = from;
			this.attributeName = name;
		}

		public String getAttributeName() {
			return attributeName;
		}

		public FromElement getFrom() {
			return from;
		}
	}

	void add(ClassDeclaration classDecl, String attributeName);

	void add(DomainDeclaration domainDecl, String attributeName);

	List<GroupByElement> getElements();
}
