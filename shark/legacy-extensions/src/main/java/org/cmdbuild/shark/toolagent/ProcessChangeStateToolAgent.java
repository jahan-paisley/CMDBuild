package org.cmdbuild.shark.toolagent;

import org.cmdbuild.workflow.SelfSuspensionRequestHolder;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstanceState;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;

public class ProcessChangeStateToolAgent extends AbstractProcessManagementToolAgent {

	private static String ADVANCE_PROCESS_PARAM = "Complete";
	private static String NEW_STATE_EXTENDED_ATTRIBUTE = "State";

	private static String CURRENT_PROCESS_INSTANCE_ID_MAGIC_VALUE = "CURRENT";

	private enum StateParam {
		Suspend, Resume
	}

	@Override
	protected void innerInvoke() throws Exception {
		switch (getNewState()) {
		case Resume:
			resumeProcess();
			break;
		case Suspend:
			suspendProcess();
			break;
		}
	}

	private void resumeProcess() throws Exception {
		final String procInstIdToResume = getProcessInstanceIdParam();
		wapi().changeProcessInstanceState(shandle, procInstIdToResume, WMProcessInstanceState.OPEN_RUNNING);
		if (shouldAdvance()) {
			advanceOneActivity(procInstIdToResume);
		}
	}

	private void suspendProcess() throws Exception {
		final String procInstIdToSuspend = getProcessInstanceIdParam();
		if (isCurrentProcessInstance(procInstIdToSuspend)) {
			SelfSuspensionRequestHolder.add(getProcessInstanceId());
		} else {
			wapi().changeProcessInstanceState(shandle, procInstIdToSuspend,
					WMProcessInstanceState.OPEN_NOTRUNNING_SUSPENDED);
		}
	}

	private boolean isCurrentProcessInstance(final String procInstIdToSuspend) {
		return CURRENT_PROCESS_INSTANCE_ID_MAGIC_VALUE.equals(procInstIdToSuspend)
				|| getProcessInstanceId().equals(procInstIdToSuspend);
	}

	private StateParam getNewState() throws ToolAgentGeneralException {
		final String newStateString = getExtendedAttribute(NEW_STATE_EXTENDED_ATTRIBUTE);
		return StateParam.valueOf(newStateString);
	}

	private boolean shouldAdvance() {
		return getBooleanFromIntegerParameter(ADVANCE_PROCESS_PARAM);
	}
}
