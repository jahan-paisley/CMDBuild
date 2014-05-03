package org.cmdbuild.services.soap.types;

public class Workflow {
	
	private String processinstanceid;
	private int processid;
	
	public Workflow(){ }

	public String getProcessinstanceid() {
		return processinstanceid;
	}

	public void setProcessinstanceid(String processinstanceid) {
		this.processinstanceid = processinstanceid;
	}

	public int getProcessid() {
		return processid;
	}

	public void setProcessid(int processid) {
		this.processid = processid;
	}
	
}
