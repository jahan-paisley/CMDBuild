package org.cmdbuild.workflow;

import static java.lang.String.format;

import org.cmdbuild.services.soap.Private;
import org.cmdbuild.shark.toolagent.ProcessChangeStateToolAgent;
import org.cmdbuild.workflow.CMDBuildEventAuditManager.WSEventNotifier;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstanceState;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

/**
 * {@link CMDBuildEventAuditManager} to handle the WRONG and totally custom
 * legacy process parallelism. It is needed for auto-suspension by
 * {@link ProcessChangeStateToolAgent}.
 */
public class LegacyCMDBuildEventAuditManager extends DelegatingEventAuditManager {

	class WSEventNotifierAndSelfSuspensionHandler extends WSEventNotifier {

		protected WSEventNotifierAndSelfSuspensionHandler(final Private proxy, final CallbackUtilities cus) {
			super(proxy, cus);
		}

		@Override
		public void activityStarted(final ActivityInstance activityInstance) {
			super.activityStarted(activityInstance);
			if (activityInstance.isNoImplementationActivity()) {
				suspendProcessInstanceIfRequested(activityInstance);
			}
		}

		private void suspendProcessInstanceIfRequested(final ActivityInstance activityInstance) {
			final String processInstanceId = activityInstance.getProcessInstanceId();
			if (SelfSuspensionRequestHolder.remove(processInstanceId)) {
				final WMSessionHandle shandle = activityInstance.getSessionHandle();
				try {
					cus.info(shandle, format("Self-suspending process %s", processInstanceId));
					Shark.getInstance()
							.getWAPIConnection()
							.changeProcessInstanceState(shandle, processInstanceId,
									WMProcessInstanceState.OPEN_NOTRUNNING_SUSPENDED);
				} catch (final Exception e) {
					cus.error(shandle, format("Cannot suspend the current process: %s", processInstanceId), e);
				}
			}
		}
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		final Private proxy = new CusSoapProxyBuilder(cus).build();
		setEventManager(new WSEventNotifierAndSelfSuspensionHandler(proxy, cus));
	}
}
