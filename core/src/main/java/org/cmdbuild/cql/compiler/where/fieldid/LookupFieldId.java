package org.cmdbuild.cql.compiler.where.fieldid;

import org.cmdbuild.cql.CQLBuilderListener.LookupOperator;
import org.cmdbuild.cql.compiler.from.FromElement;

/**
 * An identifier which contains a lookup operator, e.g. parent(),
 * Foo.parent().Code = 'bar' <br>
 * read: the Code of the parent of the Foo lookup field is equals to 'bar'"
 */
public class LookupFieldId implements FieldId {
	FromElement from;
	String id;
	LookupOperator[] operators;

	public LookupFieldId(final String id, final LookupOperator[] operators, final FromElement from) {
		this.id = id;
		this.operators = operators;
		this.from = from;
	}

	@Override
	public String getId() {
		return id;
	}

	public LookupOperator[] getOperators() {
		return operators;
	}

	@Override
	public FromElement getFrom() {
		return from;
	}

	public LookupOperatorTree getTree() {
		LookupOperatorTree out = null;
		for (final LookupOperator op : operators) {
			if (out == null) {
				out = new LookupOperatorTree(op.getOperator(), op.getAttributeName());
			} else {
				out.setNext(op.getOperator(), op.getAttributeName());
			}
		}
		return out;
	}

	public class LookupOperatorTree {
		LookupOperatorTree child = null;
		String operator = null;
		String attributeName = null;

		public LookupOperatorTree(final String operator, final String attribute) {
			this.operator = operator;
			this.attributeName = attribute;
		}

		private void setNext(final String operator, final String attribute) {
			if (child == null) {
				child = new LookupOperatorTree(operator, attribute);
			} else {
				child.setNext(operator, attribute);
			}
		}

		public String getOperator() {
			return operator;
		}

		public String getAttributeName() {
			return attributeName;
		}

		public boolean hasChild() {
			return child != null;
		}

		public LookupOperatorTree getChild() {
			return child;
		}
	}
}
