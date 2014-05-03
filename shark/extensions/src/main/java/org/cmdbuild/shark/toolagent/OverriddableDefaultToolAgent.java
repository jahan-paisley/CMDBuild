package org.cmdbuild.shark.toolagent;

import static java.lang.String.format;

import java.io.IOException;

import org.enhydra.jxpdl.elements.ExtendedAttribute;
import org.enhydra.jxpdl.elements.ExtendedAttributes;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.RootError;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.client.wfservice.XPDLBrowser;
import org.enhydra.shark.api.common.SharkConstants;
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
import org.enhydra.shark.toolagent.AbstractToolAgent;
import org.enhydra.shark.toolagent.BshToolAgent;
import org.enhydra.shark.toolagent.DefaultToolAgent;
import org.enhydra.shark.toolagent.JavaScriptToolAgent;
import org.enhydra.shark.toolagent.ToolAgentLoader;
import org.enhydra.shark.utilities.Loader;

/**
 * A completely rewrite of the {@link DefaultToolAgent} class due to its
 * impossibility to override some behavior.
 */
public class OverriddableDefaultToolAgent extends AbstractToolAgent {

	private final static String TOOL_AGENT_CLASS_EXT_ATTR_NAME = "ToolAgentClass";

	protected static class ApplicationDefinition {

		private final String className;
		private final String name;
		private final Integer mode;

		public ApplicationDefinition(final String className, final String name, final Integer mode) {
			this.className = className;
			this.name = name;
			this.mode = mode;
		}

	}

	private static Loader loader;

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		configureDefaultToolAgentForStaticMembersOnly(cus);
	}

	private void configureDefaultToolAgentForStaticMembersOnly(final CallbackUtilities cus) throws Exception {
		new DefaultToolAgent().configure(cus);
		loader = DefaultToolAgent.getLoader();
	}

	@Override
	public void invokeApplication(final WMSessionHandle shandle, final long handle, final WMEntity appInfo,
			final WMEntity toolInfo, final String applicationName, final String procInstId, final String assId,
			final AppParameter[] parameters, final Integer appMode) throws ApplicationNotStarted,
			ApplicationNotDefined, ApplicationBusy, ToolAgentGeneralException {

		super.invokeApplication(shandle, handle, appInfo, toolInfo, applicationName, procInstId, assId, parameters,
				appMode);

		try {
			status = APP_STATUS_RUNNING;

			final ApplicationDefinition definition = applicationDefinitionFrom(firstParameterValue());
			final Class<?> toolAgentClass = toolAgentClassFor(definition);
			final ToolAgent toolAgent = ToolAgent.class.cast(toolAgentClass.newInstance());
			toolAgent.configure(cus);

			final WMSessionHandle toolAgentHandle = toolAgent.connect(this.wmci);
			invoke(toolAgent, shandle, toolAgentHandle, appInfo, toolInfo, definition, procInstId, assId, parameters);
			status = requestStatus(toolAgent, shandle, toolAgentHandle, toolInfo, procInstId, assId, parameters);
			toolAgent.disconnect(toolAgentHandle);
		} catch (final ClassNotFoundException e) {
			final String message = format("application %s terminated incorrectly, cannot find tool agent class",
					appName);
			cus.error(shandle, message, e);
			throw new ApplicationNotDefined(message, e);
		} catch (final Throwable e) {
			final String message = format("cannot execute tool agent - application %s terminated incorrectly", appName);
			cus.error(shandle, message, e);

			status = APP_STATUS_INVALID;

			if (e instanceof ToolAgentGeneralException) {
				throw (ToolAgentGeneralException) e;
			} else if (e instanceof ApplicationNotStarted) {
				throw (ApplicationNotStarted) e;
			} else if (e instanceof ApplicationNotDefined) {
				throw (ApplicationNotDefined) e;
			} else if (e instanceof ApplicationBusy) {
				throw (ApplicationBusy) e;
			} else {
				throw new RootError("unexpected error", e);
			}
		}
	}

	private String firstParameterValue() {
		return String.class.cast(parameters[0].the_value);
	}

	private ApplicationDefinition applicationDefinitionFrom(final String extAttribs) throws Exception {
		String className = null;
		String applicationName = appName;

		final ExtendedAttributes extendedAttributes = readParamsFromExtAttributes(extAttribs);
		final ExtendedAttribute extendedAttribute = extendedAttributes
				.getFirstExtendedAttributeForName(TOOL_AGENT_CLASS_EXT_ATTR_NAME);
		if (extendedAttribute != null) {
			className = extendedAttribute.getVValue();
		} else {
			if (isTaskScript()) {
				final XPDLBrowser xpdlb = Shark.getInstance().getXPDLBrowser();
				final WMEntity script = xpdlb.listEntities(shandle, toolInfo, eqFilter("Type", "Script"), true)
						.getArray()[0];
				applicationName = script.getValue();

				final WMAttribute scriptType = xpdlb.listAttributes(shandle, script, eqFilter("Name", "ScriptType"),
						true).getArray()[0];
				final String scriptTypeAsString = String.class.cast(scriptType.getValue());
				className = classNameForScriptType(scriptTypeAsString);
			}
		}

		return new ApplicationDefinition(className, applicationName, appMode);
	}

	private WMFilter eqFilter(final String name, final String value) {
		final WMFilter filter = new WMFilter(name, WMFilter.EQ, value);
		filter.setFilterType(XPDLBrowser.SIMPLE_TYPE_XPDL);
		return filter;
	}

	private boolean isTaskScript() {
		return toolInfo.getType().equals("TaskScript");
	}

	/**
	 * Returns the class name for the specified script type.
	 * 
	 * @param scriptType
	 * 
	 * @return the class name for the specified script type, <code>null</code>
	 *         if there is no available classes that can be associated with the
	 *         specified script type
	 */
	protected String classNameForScriptType(final String scriptType) {
		String className = null;
		if (SharkConstants.GRAMMAR_JAVA_SCRIPT.equals(scriptType)) {
			className = JavaScriptToolAgent.class.getName();
		} else if (SharkConstants.GRAMMAR_JAVA.equals(scriptType)) {
			className = BshToolAgent.class.getName();
		}
		return className;
	}

	private Class<?> toolAgentClassFor(final ApplicationDefinition definition) throws ClassNotFoundException,
			IOException {
		Class<?> toolAgentClass = null;
		try {
			toolAgentClass = Class.forName(definition.className);
		} catch (final ClassNotFoundException e) {
			try {
				if (loader != null) {
					cus.info(shandle, format("trying to get class %s through loader...", definition.className));
					toolAgentClass = loader.loadClass(definition.className, true);
				}
			} catch (final Exception ex) {
				cus.warn(shandle, format("loader could not load class '%s'", definition.className));
			}
			if (toolAgentClass == null) {
				cus.info(shandle,
						format("trying to get class %s through %s...", definition.className, ToolAgentLoader.class));
				toolAgentClass = ToolAgentLoader.load(cus, definition.className);
			}
		}
		return toolAgentClass;
	}

	protected void invoke(final ToolAgent toolAgent, final WMSessionHandle shandle,
			final WMSessionHandle toolAgentHandle, final WMEntity appInfo, final WMEntity toolInfo,
			final ApplicationDefinition definition, final String procInstId, final String assId,
			final AppParameter[] parameters) throws ApplicationNotStarted, ApplicationNotDefined, ApplicationBusy,
			ToolAgentGeneralException {
		toolAgent.invokeApplication( //
				shandle, //
				toolAgentHandle.getId(), //
				appInfo, //
				toolInfo, //
				definition.name, //
				procInstId, //
				assId, //
				parameters, //
				definition.mode);
	}

	protected long requestStatus(final ToolAgent toolAgent, final WMSessionHandle shandle,
			final WMSessionHandle toolAgentHandle, final WMEntity toolInfo, final String procInstId,
			final String assId, final AppParameter[] parameters) throws ApplicationBusy, InvalidToolAgentHandle,
			InvalidWorkitem, InvalidProcessInstance, ToolAgentGeneralException {
		return toolAgent.requestAppStatus( //
				shandle, //
				toolAgentHandle.getId(), //
				toolInfo, //
				procInstId, //
				assId, //
				parameters);
	}

}
