package org.cmdbuild.workflow.api;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.api.fluent.ws.EntryTypeAttribute;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;
import org.cmdbuild.workflow.type.LookupType;

/**
 * API to query the database structure.
 */
public interface SchemaApi {

	/**
	 * Temporary object till we find a decent solution
	 */
	class ClassInfo {

		private final String name;
		private final int id;

		private transient final String toString;

		public ClassInfo(final String name, final int id) {
			this.name = name;
			this.id = id;
			this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		public String getName() {
			return name;
		}

		public int getId() {
			return id;
		}

		@Override
		public int hashCode() {
			return Integer.valueOf(id).hashCode();
		}

		@Override
		public boolean equals(final Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			final ClassInfo other = (ClassInfo) obj;
			return (id == other.id);
		}

		@Override
		public String toString() {
			return toString;
		}

	}

	interface AttributeInfo {

		String getName();

		WsType getWsType();

	}

	ClassInfo findClass(String className);

	ClassInfo findClass(int classId);

	AttributeInfo findAttributeFor(EntryTypeAttribute entryTypeAttribute);

	LookupType selectLookupById(int id);

	LookupType selectLookupByCode(String type, String code);

	LookupType selectLookupByDescription(String type, String description);
}
