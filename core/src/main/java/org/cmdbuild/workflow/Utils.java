package org.cmdbuild.workflow;

import java.util.Map;

import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.enhydra.shark.api.common.SharkConstants;

import com.google.common.collect.Maps;

class Utils {

	private Utils() {
		// prevents instantiation
	}

	public static final String FLOW_STATUS_LOOKUP = "FlowStatus";

	private static final Map<String, WSProcessInstanceState> stateCodeToEnumMap;

	static {
		stateCodeToEnumMap = Maps.newHashMap();
		stateCodeToEnumMap.put(SharkConstants.STATE_OPEN_RUNNING, WSProcessInstanceState.OPEN);
		stateCodeToEnumMap.put(SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED, WSProcessInstanceState.SUSPENDED);
		stateCodeToEnumMap.put(SharkConstants.STATE_CLOSED_COMPLETED, WSProcessInstanceState.COMPLETED);
		stateCodeToEnumMap.put(SharkConstants.STATE_CLOSED_TERMINATED, WSProcessInstanceState.TERMINATED);
		stateCodeToEnumMap.put(SharkConstants.STATE_CLOSED_ABORTED, WSProcessInstanceState.ABORTED);
	}

	public static WSProcessInstanceState getFlowStatusForLookup(final String flowStatusLookupCode) {
		if (flowStatusLookupCode == null) {
			return null;
		}
		final WSProcessInstanceState state = stateCodeToEnumMap.get(flowStatusLookupCode);
		return (state == null) ? WSProcessInstanceState.UNSUPPORTED : state;
	}

}
