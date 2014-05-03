package org.cmdbuild.workflow.service;

public interface WSActivityInstInfo {

	String getProcessInstanceId();

	String getActivityDefinitionId();

	String getActivityInstanceId();

	String getActivityName();

	String getActivityDescription();

	String[] getParticipants();
}
