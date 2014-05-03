package org.cmdbuild.workflow;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;

public interface WorkflowTypesConverter {

	public interface Reference {

		Long getId();

		String getClassName();

	}

	public interface Lookup {

		Long getId();

	}

	/*
	 * We should not use the {@link CMAttributeType}, but the Shark type from
	 * the XPDL. It does not handle the case when the value is null and it is
	 * not a CMDBuild attribute. It should suffice for now though.
	 */
	Object toWorkflowType(CMAttributeType<?> attributeType, Object obj);

	Object fromWorkflowType(Object obj);

}
