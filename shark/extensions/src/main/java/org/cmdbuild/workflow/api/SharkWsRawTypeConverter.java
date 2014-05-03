package org.cmdbuild.workflow.api;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.RawTypeConverter;
import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;

public class SharkWsRawTypeConverter extends SharkWsTypeConverter implements RawTypeConverter {

	public SharkWsRawTypeConverter(final WorkflowApi workflowApi) {
		super(workflowApi);
	}

	@Override
	public String toWsType(final WsType wsType, final Object value) {
		return super.toWsType(wsType, value);
	}

}
