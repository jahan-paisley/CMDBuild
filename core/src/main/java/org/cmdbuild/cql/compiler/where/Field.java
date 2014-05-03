package org.cmdbuild.cql.compiler.where;

import java.util.Collection;

import org.cmdbuild.cql.CQLBuilderListener.FieldOperator;
import org.cmdbuild.cql.CQLBuilderListener.FieldValueType;
import org.cmdbuild.cql.compiler.where.fieldid.FieldId;

public interface Field extends WhereElement {
	public class FieldValue {
		FieldValueType type;
		Object value;

		public FieldValue(final FieldValueType type) {
			this.type = type;
		}

		public void setValue(final Object value) {
			this.value = value;
		}

		public FieldValueType getType() {
			return type;
		}

		public Object getValue() {
			return value;
		}
	}

	void setId(FieldId id);

	void setOperator(FieldOperator operator);

	void nextValueType(FieldValueType type);

	void nextValue(Object value);

	FieldId getId();

	FieldOperator getOperator();

	Collection<FieldValue> getValues();
}
