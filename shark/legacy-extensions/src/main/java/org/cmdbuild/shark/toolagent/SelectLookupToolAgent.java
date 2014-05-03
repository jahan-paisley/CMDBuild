package org.cmdbuild.shark.toolagent;

import org.cmdbuild.workflow.type.LookupType;

public class SelectLookupToolAgent extends AbstractConditionalToolAgent {

	private static final String LOOKUP_ID = "LookupId";
	private static final String TYPE = "Type";
	private static final String CODE = "Code";
	private static final String DESCRIPTION = "Description";
	private static final String OUTPUT_LOOKUP = "Lookup";
	private static final String OUTPUT_STRING = "LookupDescription";

	@Override
	protected void innerInvoke() throws Exception {
		final LookupType lookup;
		if (hasParameter(LOOKUP_ID)) {
			lookup = selectById();
		} else if (hasParameter(CODE)) {
			lookup = selectByCode();
		} else {
			lookup = selectByDescription();
		}

		setOutput(lookup);
	}

	private LookupType selectById() {
		final Long id = getParameterValue(LOOKUP_ID);
		return getWorkflowApi().selectLookupById(id.intValue());
	}

	private LookupType selectByCode() {
		final String type = getParameterValue(TYPE);
		final String code = getParameterValue(CODE);
		return getWorkflowApi().selectLookupByCode(type, code);
	}

	private LookupType selectByDescription() {
		final String type = getParameterValue(TYPE);
		final String description = getParameterValue(DESCRIPTION);
		return getWorkflowApi().selectLookupByDescription(type, description);
	}

	private void setOutput(final LookupType lookup) {
		if (hasParameter(OUTPUT_LOOKUP)) {
			setParameterValue(OUTPUT_LOOKUP, lookup);
		}
		if (hasParameter(OUTPUT_STRING)) {
			setParameterValue(OUTPUT_STRING, lookup.getDescription());
		}
	}
}
