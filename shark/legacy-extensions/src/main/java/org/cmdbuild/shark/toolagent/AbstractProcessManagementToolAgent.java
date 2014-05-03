package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;

import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMActivityInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.common.ActivityFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;

public abstract class AbstractProcessManagementToolAgent extends AbstractConditionalToolAgent {

	protected static final String PROCESS_INSTANCE_ID_PARAM = "ProcessInstanceId";

	protected final void advanceOneActivity(final String procInstIdToAdvance) throws Exception {
		final WMActivityInstance actInst = getFirstOpenActivityInstance(procInstIdToAdvance);
		advanceActivity(actInst);
	}

	private void advanceActivity(final WMActivityInstance actInst) throws Exception {
		if (actInst != null) {
			if (actInst.getState() != WMActivityInstanceState.OPEN_RUNNING) {
				wapi().changeActivityInstanceState(shandle, actInst.getProcessInstanceId(), actInst.getId(),
						WMActivityInstanceState.OPEN_RUNNING);
			}
			wapi().changeActivityInstanceState(shandle, actInst.getProcessInstanceId(), actInst.getId(),
					WMActivityInstanceState.CLOSED_COMPLETED);
		}
	}

	protected final WMActivityInstance getFirstOpenActivityInstance(final String procInstIdToQuery) throws Exception {
		final WMFilter filter = openActivitiesForProcessInstance(procInstIdToQuery);
		final WMActivityInstance[] activities = wapi().listActivityInstances(shandle, filter, false).getArray();
		if (activities.length < 1) {
			cus.warn(shandle, format("No activities for process %s", procInstIdToQuery));
			return null;
		} else {
			final WMActivityInstance firstActivity = activities[0];
			if (activities.length > 1) {
				cus.warn(shandle, format("More than one activity to advance: picking %s", firstActivity.getId()));
			} else {
				cus.debug(shandle, format("Advancing activity %s", firstActivity.getId()));
			}
			return firstActivity;
		}
	}

	protected final WMFilter openActivitiesForProcessInstance(final String resumedProcInstId) throws Exception {
		final ActivityFilterBuilder fb = Shark.getInstance().getActivityFilterBuilder();
		return fb.and(shandle, fb.addProcessIdEquals(shandle, resumedProcInstId),
				fb.addStateStartsWith(shandle, SharkConstants.STATEPREFIX_OPEN));
	}

	protected final String getProcessInstanceIdParam() {
		return getParameterValue(PROCESS_INSTANCE_ID_PARAM);
	}
}
