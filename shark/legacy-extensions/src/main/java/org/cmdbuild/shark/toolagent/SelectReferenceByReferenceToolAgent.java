package org.cmdbuild.shark.toolagent;

public class SelectReferenceByReferenceToolAgent extends ManageAttributeToolAgent {

	private static final String OUT_REF = "OutRef";

	@Override
	protected String outputName() {
		return OUT_REF;
	}

}
