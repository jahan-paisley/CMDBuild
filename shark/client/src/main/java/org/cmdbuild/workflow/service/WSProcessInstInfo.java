package org.cmdbuild.workflow.service;

public interface WSProcessInstInfo extends WSProcessDefInfo {

	String getProcessInstanceId();

	WSProcessInstanceState getStatus();
}
