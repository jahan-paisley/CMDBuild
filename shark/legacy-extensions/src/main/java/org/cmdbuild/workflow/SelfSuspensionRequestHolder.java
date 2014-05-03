package org.cmdbuild.workflow;

import java.util.HashSet;
import java.util.Set;

public class SelfSuspensionRequestHolder {

	static Set<String> processInstancesToSuspend;

	static {
		processInstancesToSuspend = new HashSet<String>();
	}

	static public void add(final String processInstanceId) {
		synchronized (processInstancesToSuspend) {
			processInstancesToSuspend.add(processInstanceId);
		}
	}

	static public boolean remove(final String processInstanceId) {
		synchronized (processInstancesToSuspend) {
			return processInstancesToSuspend.remove(processInstanceId);
		}
	}
}
