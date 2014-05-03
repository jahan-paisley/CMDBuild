package org.cmdbuild.workflow.api;

import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public interface SharkWorkflowApiFactory {

	void setup(CallbackUtilities cus);

	void setup(CallbackUtilities cus, WMSessionHandle shandle, String procInstId);

	WorkflowApi createWorkflowApi();

}
