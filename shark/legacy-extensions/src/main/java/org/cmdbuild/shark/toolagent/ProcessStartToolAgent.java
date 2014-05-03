package org.cmdbuild.shark.toolagent;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.NewProcessInstance;
import org.cmdbuild.api.fluent.ProcessInstanceDescriptor;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class ProcessStartToolAgent extends ManageCardToolAgent {

	protected static final String PROCESS_CLASS_NAME = "ProcessClass";
	protected static final String ADVANCE_PROCESS_1 = "Complete";
	protected static final String ADVANCE_PROCESS_2 = "Save";

	@Override
	protected void innerInvoke() throws Exception {
		final ProcessInstanceDescriptor processInstance = startProcessInstance();
		fillOutputParameters(processInstance);
	}

	private void fillOutputParameters(final ProcessInstanceDescriptor processInstanceDescriptor) {
		for (final AppParameter parmOut : getReturnParameters()) {
			if (parmOut.the_class == Long.class) {
				parmOut.the_value = processInstanceDescriptor.getId().longValue();
			} else if (parmOut.the_class == String.class) {
				parmOut.the_value = processInstanceDescriptor.getProcessInstanceId();
			}
		}
	}

	private ProcessInstanceDescriptor startProcessInstance() {
		final NewProcessInstance newProcessInstance = getWorkflowApi().newProcessInstance(getProcessClassName());
		for (final Entry<String, Object> attribute : getAttributeMap().entrySet()) {
			newProcessInstance.with(attribute.getKey(), attribute.getValue());
		}
		if (shouldAdvance()) {
			return newProcessInstance.startAndAdvance();
		} else {
			return newProcessInstance.start();
		}
	}

	private final String getProcessClassName() {
		return getExtendedAttribute(PROCESS_CLASS_NAME);
	}

	private boolean shouldAdvance() {
		return getBooleanFromIntegerExtendedAttribute(ADVANCE_PROCESS_1)
				|| getBooleanFromIntegerExtendedAttribute(ADVANCE_PROCESS_2);
	}

	@Override
	protected List<String> fixedMetaAttributeNames() {
		return emptyList();
	}

}
