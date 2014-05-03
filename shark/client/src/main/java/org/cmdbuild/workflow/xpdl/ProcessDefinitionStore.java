package org.cmdbuild.workflow.xpdl;

import java.util.List;

import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.service.WSProcessDefInfo;

public interface ProcessDefinitionStore {

	List<CMActivity> getManualStartActivities(String className) throws CMWorkflowException;

	CMActivity getActivity(WSProcessDefInfo procDefInfo, String activityDefinitionId) throws CMWorkflowException;

	String[] getPackageVersions(String className) throws CMWorkflowException;

	String getPackageId(String className) throws CMWorkflowException;

	String getProcessDefinitionId(String className) throws CMWorkflowException;

	String getProcessClassName(String processDefinitionId) throws CMWorkflowException;

	byte[] downloadPackage(String className, String pkgVer) throws CMWorkflowException;

	void uploadPackage(String className, byte[] pkgDefData) throws CMWorkflowException;

}