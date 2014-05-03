package org.cmdbuild.servlets.json.management;

import org.cmdbuild.exception.NotFoundException;
import org.cmdbuild.exception.NotFoundException.NotFoundExceptionType;
import org.cmdbuild.logger.Log;

public class ActivityIdentifier {

	String processInstanceId;
	String workItemId;
	public ActivityIdentifier(String pii,String wii) throws NotFoundException{
		this.processInstanceId=pii;
		this.workItemId=wii;
		check();
		// TODO: try to resolve the '#' url encoding error, this is a brutal hack.. (substitude '#' with '__')
		if( -1 == workItemId.indexOf('#') ) {
			this.workItemId = workItemId.replace("__", "#");
		}
		Log.WORKFLOW.debug("ProcessInstanceId: " + processInstanceId + ", WorkItemId: " + workItemId);
	}
	public void check() throws NotFoundException {
		if (processInstanceId == null || workItemId == null){
			throw NotFoundExceptionType.NOTFOUND.createException();
		}
	}
	
	public String getProcessInstanceId() {
		return processInstanceId;
	}
	public String getWorkItemId() {
		return workItemId;
	}
}
