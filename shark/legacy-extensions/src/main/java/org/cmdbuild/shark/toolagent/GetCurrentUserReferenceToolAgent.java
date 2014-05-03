package org.cmdbuild.shark.toolagent;

import org.cmdbuild.workflow.Constants;
import org.cmdbuild.workflow.type.ReferenceType;

public class GetCurrentUserReferenceToolAgent extends AbstractConditionalToolAgent {

	private static final String OUTPUT = "UserRef";

	@Override
	protected void innerInvoke() throws Exception {
		final ReferenceType userReference = getProcessAttributeValue(Constants.CURRENT_USER_VARIABLE);
		setParameterValue(OUTPUT, userReference);
	}

}
