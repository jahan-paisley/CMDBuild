package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.enhydra.jxpdl.XPDLConstants.FORMAL_PARAMETER_MODE_INOUT;
import static org.enhydra.jxpdl.XPDLConstants.FORMAL_PARAMETER_MODE_OUT;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.toolagent.BshToolAgent;

public class GroovyToolAgent extends AbstractConditionalToolAgent {

	private static final String GROOVY_SCRIPT_REPOSITORY = "CMDBuild.Groovy.Repository";

	private static final List<String> OUTPUT_MODES = asList(FORMAL_PARAMETER_MODE_OUT, FORMAL_PARAMETER_MODE_INOUT);

	@Override
	protected void innerInvoke() throws Exception {
		try {
			status = APP_STATUS_RUNNING;
			if (evaluateFile()) {
				if (modeIsFile()) {
					runFileNamedAsAppName();
				}
			} else {
				evaluateScriptWithinExtendedAttributes();
			}
			status = APP_STATUS_FINISHED;
		} catch (final Throwable e) {
			status = APP_STATUS_INVALID;
			final String message = format("error executing %s", appName);
			cus.error(shandle, message);
			throw new ToolAgentGeneralException(e);
		}
	}

	private boolean evaluateFile() {
		return isNotBlank(appName);
	}

	private boolean modeIsFile() {
		return (appMode != null) && (appMode == BshToolAgent.APP_MODE_FILE);
	}

	private void runFileNamedAsAppName() throws IOException, ResourceException, ScriptException {
		final Binding binding = bindingFor();
		final List<String> repositories = repositories();
		cus.debug(shandle, format("groovy script repositories: %s", repositories));
		final String[] reporitoriesAsArray = repositories.toArray(new String[repositories.size()]);
		final GroovyScriptEngine gse = new GroovyScriptEngine(reporitoriesAsArray);
		gse.run(appName, binding);
		getResultFromEvaluation(binding);
	}

	protected List<String> repositories() {
		final List<String> reporitories = new ArrayList<String>();
		final String repositoryFromProperties = cus.getProperty(GROOVY_SCRIPT_REPOSITORY);
		if (isNotBlank(repositoryFromProperties)) {
			reporitories.add(repositoryFromProperties);
		}
		return reporitories;
	}

	protected void evaluateScriptWithinExtendedAttributes() throws Exception {
		final Binding binding = bindingFor();
		final GroovyShell shell = new GroovyShell(binding);
		shell.evaluate(scriptFromExtendedAttributes());
		getResultFromEvaluation(binding);
	}

	private String scriptFromExtendedAttributes() throws Exception {
		final String script = getExtendedAttribute(BshToolAgent.SCRIPT_EXT_ATTR_NAME);
		cus.debug(shandle, format("'%s' extended attribute value is '%s'", BshToolAgent.SCRIPT_EXT_ATTR_NAME, script));
		if (script != null) {
			return script;
		}
		throw new IllegalArgumentException("missing script");
	}

	private Binding bindingFor() {
		final Binding binding = new Binding();
		if (parameters != null) {
			// Shark workaround: ignore 1st param because "it is ext. attribs"
			for (final AppParameter parameter : parameters) {
				// Getting formal parameters (input parameters)
				final String key = parameter.the_formal_name;
				final java.lang.Object value = parameter.the_value;
				binding.setVariable(key, value);
			}
		}
		return binding;
	}

	private void getResultFromEvaluation(final Binding binding) {
		final List<AppParameter> parameters = (this.parameters == null) ? Collections.<AppParameter> emptyList()
				: asList(super.parameters);
		for (final AppParameter parameter : parameters) {
			if (OUTPUT_MODES.contains(parameter.the_mode)) {
				parameter.the_value = binding.getVariable(parameter.the_formal_name);
				// Conversion to maintain Shark compatibility
				if (parameter.the_value instanceof Integer) {
					parameter.the_value = new Long(((Integer) parameter.the_value).intValue());
				}
			}
		}
	}

}