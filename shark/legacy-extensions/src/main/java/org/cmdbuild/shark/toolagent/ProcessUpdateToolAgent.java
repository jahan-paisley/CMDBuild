package org.cmdbuild.shark.toolagent;

import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class ProcessUpdateToolAgent extends AbstractProcessManagementToolAgent {

	protected static final String ADVANCE_PROCESS = "Complete";

	@Override
	protected void innerInvoke() throws Exception {
		final String procInstIdToUpdate = getProcessInstanceIdParam();
		updateProcessInstanceParameters(procInstIdToUpdate);
		if (shouldAdvance()) {
			advanceOneActivity(procInstIdToUpdate);
		}
	}

	private void updateProcessInstanceParameters(final String procInstIdToUpdate) throws Exception {
		for (final AppParameter p : getInputParameters()) {
			final String attrName = p.the_formal_name;
			final Object attrValue = p.the_value;
			if (PROCESS_INSTANCE_ID_PARAM.equals(p.the_formal_name)) {
				continue;
			}
			wapi().assignProcessInstanceAttribute(shandle, procInstIdToUpdate, attrName, attrValue);
		}
	}

	private boolean shouldAdvance() {
		return getBooleanFromIntegerExtendedAttribute(ADVANCE_PROCESS);
	}
}
