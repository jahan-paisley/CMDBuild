package org.cmdbuild.workflow.service;

/**
 * Process states that we care about.
 */
public enum WSProcessInstanceState {
	OPEN, SUSPENDED, COMPLETED, TERMINATED, ABORTED, UNSUPPORTED
}
