package org.cmdbuild.shark.toolagent;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.cmdbuild.workflow.ConfigurationHelper;
import org.cmdbuild.workflow.Constants;
import org.cmdbuild.workflow.api.SharkWorkflowApiFactory;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.InvalidProcessInstance;
import org.enhydra.shark.api.internal.toolagent.InvalidToolAgentHandle;
import org.enhydra.shark.api.internal.toolagent.InvalidWorkitem;
import org.enhydra.shark.api.internal.toolagent.ToolAgent;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.toolagent.DefaultToolAgent;

/**
 * A replacement for {@link DefaultToolAgent} class.
 * 
 * This implementation:
 * <ul>
 * <li>is needed for Shark 4.4 only</li>
 * <li>supports Groovy scripts</li>
 * <li>injects CMDBuild APIs as script parameters</li>
 * <ul>
 */
public class CmdbuildDefaultToolAgent extends OverriddableDefaultToolAgent {

	private static final String TEXT_GROOVY = "text/groovy";

	private SharkWorkflowApiFactory workflowApiFactory;

	private volatile WorkflowApi workflowApi;

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		final ConfigurationHelper helper = new ConfigurationHelper(cus);
		workflowApiFactory = helper.getWorkflowApiFactory();
	}

	@Override
	protected String classNameForScriptType(final String scriptType) {
		final String className = super.classNameForScriptType(scriptType);
		if ((className == null) && TEXT_GROOVY.equals(scriptType)) {
			return GroovyToolAgent.class.getName();
		}
		return className;
	}

	@Override
	protected void invoke(final ToolAgent toolAgent, final WMSessionHandle shandle,
			final WMSessionHandle toolAgentHandle, final WMEntity appInfo, final WMEntity toolInfo,
			final ApplicationDefinition definition, final String procInstId, final String assId,
			final AppParameter[] parameters) throws ApplicationNotStarted, ApplicationNotDefined, ApplicationBusy,
			ToolAgentGeneralException {
		final ParametersLogger parametersLogger = ParametersLogger.from(cus, shandle);
		workflowApiFactory.setup(cus, shandle, procInstId);
		parametersLogger.beforeInvocation(parameters);
		super.invoke( //
				toolAgent, //
				shandle, //
				toolAgentHandle, //
				appInfo, //
				toolInfo, //
				definition, //
				procInstId, //
				assId, //
				parametersForInvocation(parameters));
		parametersLogger.afterInvocation(parameters);
	}

	@Override
	protected long requestStatus(final ToolAgent toolAgent, final WMSessionHandle shandle,
			final WMSessionHandle toolAgentHandle, final WMEntity toolInfo, final String procInstId,
			final String assId, final AppParameter[] parameters) throws ApplicationBusy, InvalidToolAgentHandle,
			InvalidWorkitem, InvalidProcessInstance, ToolAgentGeneralException {
		workflowApiFactory.setup(cus, shandle, procInstId);
		return super.requestStatus(toolAgent, //
				shandle, //
				toolAgentHandle, //
				toolInfo, //
				procInstId, //
				assId, //
				parametersForInvocation(parameters));
	}

	private AppParameter[] parametersForInvocation(final AppParameter[] parameters) {
		final List<AppParameter> appParameters = new ArrayList<AppParameter>(asList(parameters));
		appParameters.add(apiParameter());
		return appParameters.toArray(new AppParameter[appParameters.size()]);
	}

	private AppParameter apiParameter() {
		if (workflowApi == null) {
			synchronized (this) {
				if (workflowApi == null) {
					/*
					 * TODO add some kind of proxy
					 * 
					 * for have the api initialized only when needed (e.g. first
					 * method called)
					 */

					workflowApi = workflowApiFactory.createWorkflowApi();
				}
			}
		}
		return new AppParameter( //
				Constants.API_VARIABLE, //
				Constants.API_VARIABLE, //
				XPDLConstants.FORMAL_PARAMETER_MODE_IN, //
				workflowApi, //
				workflowApi.getClass());
	}

}
