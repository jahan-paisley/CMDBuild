package org.cmdbuild.workflow.api;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.cmdbuild.workflow.Constants.CURRENT_GROUP_NAME_VARIABLE;
import static org.cmdbuild.workflow.Constants.CURRENT_USER_USERNAME_VARIABLE;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor;
import org.cmdbuild.common.mail.MailApi;
import org.cmdbuild.common.mail.MailApiFactory;
import org.cmdbuild.common.utils.UnsupportedProxyFactory;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.workflow.ConfigurationHelper;
import org.cmdbuild.workflow.CusSoapProxyBuilder;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMAttribute;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class SoapSharkWorkflowApiFactory implements SharkWorkflowApiFactory {

	private static MailApi NULL_MAIL_API = UnsupportedProxyFactory.of(MailApi.class).create();

	private static class ProcessData {

		public final WMSessionHandle shandle;
		public final String procInstId;

		public ProcessData(final WMSessionHandle shandle, final String procInstId) {
			this.shandle = shandle;
			this.procInstId = procInstId;
		}

	}

	private CallbackUtilities cus;
	private ProcessData processData;

	@Override
	public void setup(final CallbackUtilities cus) {
		setup(cus, null);
	}

	@Override
	public void setup(final CallbackUtilities cus, final WMSessionHandle shandle, final String procInstId) {
		setup(cus, new ProcessData(shandle, procInstId));
	}

	private void setup(final CallbackUtilities cus, final ProcessData processData) {
		this.cus = cus;
		this.processData = processData;
	}

	@Override
	public WorkflowApi createWorkflowApi() {
		final Private proxy = proxy();
		final CachedWsSchemaApi schemaApi = new CachedWsSchemaApi(proxy);
		final WsFluentApiExecutor wsFluentApiExecutor = new WsFluentApiExecutor(proxy);
		final WorkflowApi workflowApi = new WorkflowApi(wsFluentApiExecutor, schemaApi, mailApi());

		// FIXME needed for cut-off circular dependency
		wsFluentApiExecutor.setEntryTypeConverter(new SharkWsEntryTypeConverter(workflowApi));
		wsFluentApiExecutor.setRawTypeConverter(new SharkWsRawTypeConverter(workflowApi));

		return workflowApi;
	}

	private Private proxy() {
		return new CusSoapProxyBuilder(cus) //
				.withUsername(currentUserOrEmptyOnError()) //
				.withGroup(currentGroupOrEmptyOnError()) //
				.build();
	}

	private String currentUserOrEmptyOnError() {
		if (processData == null) {
			return EMPTY;
		}

		try {
			final WMAttribute attribute = wapi().getProcessInstanceAttributeValue(processData.shandle,
					processData.procInstId, CURRENT_USER_USERNAME_VARIABLE);
			final Object value = attribute.getValue();
			return String.class.cast(value);
		} catch (final Throwable e) {
			return EMPTY;
		}
	}

	private String currentGroupOrEmptyOnError() {
		if (processData == null) {
			return EMPTY;
		}

		try {
			final WMAttribute attribute = wapi().getProcessInstanceAttributeValue(processData.shandle,
					processData.procInstId, CURRENT_GROUP_NAME_VARIABLE);
			final Object value = attribute.getValue();
			return String.class.cast(value);
		} catch (final Throwable e) {
			return EMPTY;
		}
	}

	private WAPI wapi() throws Exception {
		return Shark.getInstance().getWAPIConnection();
	}

	private MailApi mailApi() {
		try {
			final ConfigurationHelper helper = new ConfigurationHelper(cus);
			final MailApi.Configuration mailApiConfiguration = helper.getMailApiConfiguration();
			final MailApiFactory mailApiFactory = helper.getMailApiFactory();
			mailApiFactory.setConfiguration(mailApiConfiguration);
			return mailApiFactory.createMailApi();
		} catch (final Exception e) {
			return NULL_MAIL_API;
		}
	}

}
