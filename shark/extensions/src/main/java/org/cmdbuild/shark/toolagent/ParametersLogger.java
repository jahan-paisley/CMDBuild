package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;

import java.util.Arrays;

import org.cmdbuild.shark.Logging;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class ParametersLogger {

	public static final ParametersLogger from(final CallbackUtilities cus, final WMSessionHandle sessionHandle) {
		return new ParametersLogger(cus, sessionHandle);
	}

	private final CallbackUtilities callbackUtilities;
	private final WMSessionHandle sessionHandle;

	public ParametersLogger(final CallbackUtilities callbackUtilities, final WMSessionHandle sessionHandle) {
		this.callbackUtilities = callbackUtilities;
		this.sessionHandle = sessionHandle;
	}

	public void beforeInvocation(AppParameter[] parameters) {
		callbackUtilities.debug(sessionHandle, Logging.LOGGER_NAME, "parameters before invocation...");
		dumpParameters(parameters);
	}

	public void afterInvocation(AppParameter[] parameters) {
		callbackUtilities.debug(sessionHandle, Logging.LOGGER_NAME, "parameters after invocation...");
		dumpParameters(parameters);
	}

	private void dumpParameters(final AppParameter[] parameters) {
		for (final AppParameter parameter : parameters) {
			callbackUtilities.debug(sessionHandle, Logging.LOGGER_NAME, formatParameter(parameter));
		}
	}

	private String formatParameter(final AppParameter parameter) {
		final Object value;
		if (parameter.the_class.isArray()) {
			value = Arrays.toString((Object[]) parameter.the_value);
		} else {
			value = parameter.the_value;
		}
		return format("parameter '%s' (%s) = '%s'", //
				parameter.the_formal_name, parameter.the_class, value);
	}

}
