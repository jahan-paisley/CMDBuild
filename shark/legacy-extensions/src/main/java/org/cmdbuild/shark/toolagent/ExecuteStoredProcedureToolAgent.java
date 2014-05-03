package org.cmdbuild.shark.toolagent;

import java.util.Map;
import java.util.Map.Entry;

import org.cmdbuild.api.fluent.FunctionCall;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.enhydra.shark.api.internal.toolagent.AppParameter;

/**
 * Executes a function.
 * 
 * {@link ReferenceType} and {@link LookupType} are converted to and from integers.
 * 
 */
public class ExecuteStoredProcedureToolAgent extends AbstractConditionalToolAgent {

	private final String PROCEDURE = "Procedure";
	private final String CURSOR_PROCEDURE = "CursorProcedure";

	@Override
	protected void innerInvoke() throws Exception {
		final String functionName = getFunctionName();
		final Map<String, Object> input = getInputParameterValues();
		final Map<String, Object> output = callFunction(functionName, input);

		for (final AppParameter parmOut : getReturnParameters()) {
			Object outputValue = output.get(parmOut.the_formal_name);
			if (parmOut.the_class == ReferenceType.class) {
				outputValue = getWorkflowApi().referenceTypeFrom(outputValue);
			} else if (parmOut.the_class == LookupType.class) {
				Number lookupId = (Number) outputValue;
				outputValue = getWorkflowApi().selectLookupById(lookupId.intValue());
			}
			parmOut.the_value = outputValue;
		}
	}

	private Map<String, Object> callFunction(final String functionName, final Map<String, Object> input) {
		final FunctionCall callFunction = getWorkflowApi().callFunction(functionName);
		for (final Entry<String, Object> entry : input.entrySet()) {
			final Object normalizedValue = convertReferenceAndLookupToIntegers(entry.getValue());
			callFunction.with(entry.getKey(), normalizedValue);
		}
		final Map<String, Object> output = callFunction.execute();
		return output;
	}

	private String getFunctionName() {
		String functionName = getExtendedAttribute(PROCEDURE);
		if (functionName == null) {
			functionName = getExtendedAttribute(CURSOR_PROCEDURE);
		}
		return functionName;
	}

	protected final Object convertReferenceAndLookupToIntegers(Object obj) {
		if (obj != null) {
			if (obj instanceof ReferenceType) {
				final ReferenceType ref = (ReferenceType) obj;
				if (ref.checkValidity()) {
					obj = Long.valueOf(ref.getId());
				} else {
					return null;
				}
			} else if (obj instanceof LookupType) {
				final LookupType loo = (LookupType) obj;
				if (loo.checkValidity()) {
					obj = Long.valueOf(loo.getId());
				} else {
					return null;
				}
			}
		}
		return obj;
	}
}
