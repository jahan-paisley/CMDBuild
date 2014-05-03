package org.cmdbuild.workflow.service;

import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstanceState;
import org.enhydra.shark.utilities.MiscUtilities;

public class WSProcessInstInfoImpl extends WSProcessDefInfoImpl implements WSProcessInstInfo {

	protected String processInstanceId;
	protected WSProcessInstanceState status;

	static WSProcessInstInfo newInstance(final WMProcessInstance processInstance) {
		if (processInstance == null)
			return null;
		final WSProcessInstInfoImpl instance = new WSProcessInstInfoImpl();
		instance.packageId = MiscUtilities.getProcessMgrPkgId(processInstance.getProcessFactoryName());
		instance.packageVersion = MiscUtilities.getProcessMgrVersion(processInstance.getProcessFactoryName());
		instance.processDefinitionId = processInstance.getProcessDefinitionId();
		instance.processInstanceId = processInstance.getId();
		instance.status = convertStatus(processInstance.getState());
		return instance;
	}

	private static WSProcessInstanceState convertStatus(final WMProcessInstanceState state) {
		if (state == null) {
			// We have no control over this field, so it's
			// best to assume that it might be null.
			return WSProcessInstanceState.UNSUPPORTED;
		}
		switch (state.value()) {
		case WMProcessInstanceState.OPEN_RUNNING_INT:
			return WSProcessInstanceState.OPEN;
		case WMProcessInstanceState.CLOSED_COMPLETED_INT:
			return WSProcessInstanceState.COMPLETED;
		case WMProcessInstanceState.CLOSED_ABORTED_INT:
			return WSProcessInstanceState.ABORTED;
		case WMProcessInstanceState.CLOSED_TERMINATED_INT:
			return WSProcessInstanceState.TERMINATED;
		case WMProcessInstanceState.OPEN_NOTRUNNING_SUSPENDED_INT:
			return WSProcessInstanceState.SUSPENDED;
		case WMProcessInstanceState.OPEN_NOTRUNNING_NOTSTARTED_INT:
		default:
			return WSProcessInstanceState.UNSUPPORTED;
		}
	}

	@Override
	public String getProcessInstanceId() {
		return processInstanceId;
	}

	@Override
	public WSProcessInstanceState getStatus() {
		return status;
	}

}
