package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.ws.EntryTypeAttribute;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.EntryTypeConverter;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;
import org.cmdbuild.workflow.api.SchemaApi.AttributeInfo;

public class SharkWsEntryTypeConverter extends SharkWsTypeConverter implements EntryTypeConverter {

	public SharkWsEntryTypeConverter(final WorkflowApi workflowApi) {
		super(workflowApi);
	}

	@Override
	public String toWsType(final EntryTypeAttribute entryTypeAttribute, final Object clientValue) {
		return toWsType(getWsType(entryTypeAttribute), clientValue);
	}

	@Override
	public Object toClientType(final EntryTypeAttribute entryTypeAttribute, final String wsValue) {
		return toClientType(getWsType(entryTypeAttribute), wsValue);
	}

	private WsType getWsType(final EntryTypeAttribute entryTypeAttribute) {
		final AttributeInfo attributeInfo = workflowApi.findAttributeFor(entryTypeAttribute);
		return attributeInfo.getWsType();
	}

}
