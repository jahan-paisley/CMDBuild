package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cmdbuild.workflow.ConfigurationHelper;
import org.cmdbuild.workflow.api.SharkWorkflowApiFactory;
import org.cmdbuild.workflow.api.WorkflowApi;
import org.enhydra.jxpdl.XMLUtil;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.internal.toolagent.AppParameter;
import org.enhydra.shark.api.internal.toolagent.ApplicationBusy;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotDefined;
import org.enhydra.shark.api.internal.toolagent.ApplicationNotStarted;
import org.enhydra.shark.api.internal.toolagent.ToolAgentGeneralException;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.toolagent.AbstractToolAgent;

public abstract class AbstractConditionalToolAgent extends AbstractToolAgent {

	private static final String EXTENDED_ATTRIBUTES_PARAM = "ExtendedAttributes";

	public interface ConditionEvaluator {

		void configure(CallbackUtilities cus) throws Exception;

		boolean evaluate();

	}

	private final ConditionEvaluator conditionEvaluator;

	private SharkWorkflowApiFactory workflowApiFactory;
	private volatile WorkflowApi workflowApi;

	public AbstractConditionalToolAgent() {
		conditionEvaluator = new SharkConditionalEvaluator(this);
	}

	public AbstractConditionalToolAgent(final ConditionEvaluator conditionEvaluator) {
		this.conditionEvaluator = conditionEvaluator;
	}

	WMSessionHandle getSessionHandle() {
		return shandle;
	}

	WMEntity getToolInfo() {
		return toolInfo;
	}

	String getProcessInstanceId() {
		return procInstId;
	}

	public String getAssId() {
		return assId;
	}

	public String getId() {
		return toolInfo.getId();
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);

		conditionEvaluator.configure(cus);

		final ConfigurationHelper configurationHelper = new ConfigurationHelper(cus);
		workflowApiFactory = configurationHelper.getWorkflowApiFactory();
	}

	@Override
	public void invokeApplication(final WMSessionHandle shandle, final long handle, final WMEntity appInfo,
			final WMEntity toolInfo, final String applicationName, final String procInstId, final String assId,
			final AppParameter[] parameters, final Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {
		super.invokeApplication(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters,
				appMode);
		setStatus(APP_STATUS_RUNNING);
		try {
			if (conditionEvaluator.evaluate()) {
				final ParametersLogger parametersLogger = ParametersLogger.from(cus, shandle);
				parametersLogger.beforeInvocation(parameters);
				setupWorkflowApi(shandle, procInstId);
				innerInvoke();
				parametersLogger.afterInvocation(parameters);
			}
			setStatus(APP_STATUS_FINISHED);
		} catch (final Exception e) {
			setStatus(APP_STATUS_INVALID);
			throw new ToolAgentGeneralException(e);
		}
	}

	protected void setStatus(final long status) {
		this.status = status;
	}

	protected void setupWorkflowApi(final WMSessionHandle shandle, final String procInstId) {
		workflowApiFactory.setup(cus, shandle, procInstId);
	}

	public WorkflowApi getWorkflowApi() {
		if (workflowApi == null) {
			synchronized (this) {
				if (workflowApi == null) {
					workflowApi = workflowApiFactory.createWorkflowApi();
				}
			}
		}
		return workflowApi;
	}

	protected abstract void innerInvoke() throws Exception;

	@SuppressWarnings("unchecked")
	public <T> T getParameterValue(final String name) {
		final Object value = getParameterOrDie(name).the_value;
		return (T) value;
	}

	public void setParameterValue(final String name, final Object value) {
		getParameter(name).the_value = value;
	}

	private AppParameter getParameterOrDie(final String name) {
		final AppParameter parameter = getParameter(name);
		if (parameter == null) {
			throw new IllegalArgumentException(format("missing parameter for name '%s'", name));
		} else {
			return parameter;
		}
	}

	public boolean hasParameter(final String name) {
		return (getParameter(name) != null);
	}

	/**
	 * Gets the application parameter or null if not present.
	 * 
	 * @param name
	 *            parameter name
	 * @return parameter or null
	 */
	private AppParameter getParameter(final String name) {
		for (final AppParameter parameter : parameters) {
			final String formalName = parameter.the_formal_name;
			if (formalName.equals(name)) {
				return parameter;
			}
		}
		return null;
	}

	/**
	 * Get IN and INOUT parameter values
	 */
	protected final Map<String, Object> getInputParameterValues() {
		final Map<String, Object> paramMap = new HashMap<String, Object>();
		for (final AppParameter p : getInputParameters()) {
			paramMap.put(p.the_formal_name, p.the_value);
		}
		return paramMap;
	}

	protected final List<AppParameter> getInputParameters() {
		final List<AppParameter> params = new ArrayList<AppParameter>();
		for (final AppParameter p : parameters) {
			if (XPDLConstants.FORMAL_PARAMETER_MODE_OUT.equals(p.the_mode)
					|| EXTENDED_ATTRIBUTES_PARAM.equals(p.the_formal_name)) {
				continue;
			}
			params.add(p);
		}
		return params;
	}

	/**
	 * Decodes the XML because Shark 2.3 does not use jxpdl, so it would crash.
	 * 
	 * @param name
	 *            extended attribute key
	 * @return extended attribute value for that key
	 */
	protected final String getExtendedAttribute(final String name) {
		String value = null;
		try {
			final String eaXml = getParameterValue(EXTENDED_ATTRIBUTES_PARAM);
			final ExtendedAttributes eas = XMLUtil.destringyfyExtendedAttributes(eaXml);
			final ExtendedAttribute ea = eas.getFirstExtendedAttributeForName(name);
			if (ea != null) {
				value = ea.getVValue();
			}
		} catch (final Exception e) {
			cus.error(null, format("unable to get extended attribute '%s'", name));
		}
		return value;
	}

	@SuppressWarnings("unchecked")
	public <T> T getProcessAttributeValue(final String name) throws Exception {
		final WMAttribute attribute = wapi().getProcessInstanceAttributeValue(shandle, procInstId, name);
		final Object value = attribute.getValue();
		return (T) value;
	}

	protected final boolean getBooleanFromIntegerExtendedAttribute(final String name) {
		final String value = getExtendedAttribute(name);
		return value != null && "1".equals(value);
	}

	protected final boolean getBooleanFromIntegerParameter(final String name) {
		final AppParameter param = getParameter(name);
		if (param != null && Number.class.isAssignableFrom(param.the_class)) {
			final Number value = (Number) param.the_value;
			return value != null && value.intValue() == 1;
		} else {
			return false;
		}
	}

	protected final WAPI wapi() throws Exception {
		return Shark.getInstance().getWAPIConnection();
	}

}

class CardRef {
	final String className;
	final int cardId;

	public CardRef(final String className, final int cardId) {
		this.className = className;
		this.cardId = cardId;
	}
}
