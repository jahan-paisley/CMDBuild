package org.cmdbuild.shark.toolagent;

import org.cmdbuild.workflow.Constants;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

public class GetCurrentGroupReferenceToolAgent extends AbstractConditionalToolAgent {

	@Override
	protected void innerInvoke() throws Exception {
		final ReferenceType groupReference = getProcessAttributeValue(Constants.CURRENT_GROUP_VARIABLE);
		setAllReferenceOutputParametersTo(groupReference);
	}

	/**
	 * Set all reference output attributes with the current group reference.
	 * 
	 * It should have been just GroupRef but in VERY old process definitions the
	 * output parameter was GroupName so, what the hell, we don't care about the
	 * parameter name as long as it is a reference. Good boy!
	 * 
	 * @param groupReference
	 */
	private void setAllReferenceOutputParametersTo(final ReferenceType groupReference) {
		for (final AppParameter parmOut : getReturnParameters()) {
			if (parmOut.the_class == ReferenceType.class) {
				parmOut.the_value = groupReference;
			}
		}
	}

}
