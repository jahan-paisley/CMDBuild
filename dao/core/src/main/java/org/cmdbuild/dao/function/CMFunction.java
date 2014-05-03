package org.cmdbuild.dao.function;

import java.util.List;

import org.cmdbuild.dao.CMTypeObject;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public interface CMFunction extends CMTypeObject {

	enum Category {
		SYSTEM, //
		UNDEFINED, //
		;

		public static Category of(final String text) {
			for (final Category category : values()) {
				if (category.name().equalsIgnoreCase(text)) {
					return category;
				}
			}
			return UNDEFINED;
		}
	}

	interface CMFunctionParameter {

		String getName();

		CMAttributeType<?> getType();
	}

	List<CMFunctionParameter> getInputParameters();

	List<CMFunctionParameter> getOutputParameters();

	boolean returnsSet();

	Iterable<Category> getCategories();

}
