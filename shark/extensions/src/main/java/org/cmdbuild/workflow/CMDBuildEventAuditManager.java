package org.cmdbuild.workflow;

import static java.lang.String.*;

import org.cmdbuild.services.soap.AbstractWorkflowEvent;
import org.cmdbuild.services.soap.Private;
import org.cmdbuild.services.soap.ProcessStartEvent;
import org.cmdbuild.services.soap.ProcessUpdateEvent;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

/**
 * EventAuditManager that notifies CMDBuild through web services.
 */
public class CMDBuildEventAuditManager extends DelegatingEventAuditManager {

	static class WSEventNotifier implements SimpleEventManager {

		private final Private proxy;
		private final CallbackUtilities cus;

		protected WSEventNotifier(final Private proxy, final CallbackUtilities cus) {
			this.proxy = proxy;
			this.cus = cus;
		}

		@Override
		public void processStarted(final ProcessInstance processInstance) {
			cus.info(null, format("process '%s' started", //
					processInstance.getProcessDefinitionId()));
			sendProcessStartEvent(processInstance);
		}

		@Override
		public void processClosed(final ProcessInstance processInstance) {
			cus.info(null, format("process '%s' closed", //
					processInstance.getProcessDefinitionId()));
			sendProcessUpdateEvent(processInstance);
		}

		@Override
		public void processSuspended(final ProcessInstance processInstance) {
			cus.info(null, format("process '%s' suspended", //
					processInstance.getProcessDefinitionId()));
			sendProcessUpdateEvent(processInstance);
		}

		@Override
		public void processResumed(final ProcessInstance processInstance) {
			cus.info(null, format("process '%s' resumed", //
					processInstance.getProcessDefinitionId()));
			sendProcessUpdateEvent(processInstance);
		}

		@Override
		public void activityStarted(final ActivityInstance activityInstance) {
			cus.info(null, format("activity '%s' started", //
					activityInstance.getActivityDefinitionId()));
			sendProcessUpdateEventIfNoImpl(activityInstance);
		}

		@Override
		public void activityClosed(final ActivityInstance activityInstance) {
			cus.info(null, format("activity '%s' closed", //
					activityInstance.getActivityDefinitionId()));
			sendProcessUpdateEventIfNoImpl(activityInstance);
		}

		private void sendProcessUpdateEventIfNoImpl(final ActivityInstance activityInstance) {
			if (activityInstance.isNoImplementationActivity()) {
				cus.info(null, format("sending notification for activity '%s'", //
						activityInstance.getActivityDefinitionId()));
				sendProcessUpdateEvent(activityInstance);
			}
		}

		private void sendProcessUpdateEvent(final ProcessInstance processInstance) {
			cus.info(null, format("sending notification for update of process '%s'", //
					processInstance.getProcessDefinitionId()));
			final AbstractWorkflowEvent event = new ProcessUpdateEvent();
			fillEventProperties(processInstance, event);
			proxy.notify(event);
		}

		private void sendProcessStartEvent(final ProcessInstance processInstance) {
			cus.info(null, format("sending notification for start of process '%s'", //
					processInstance.getProcessDefinitionId()));
			final AbstractWorkflowEvent event = new ProcessStartEvent();
			fillEventProperties(processInstance, event);
			proxy.notify(event);
		}

		private void fillEventProperties(final ProcessInstance processInstance,
				final AbstractWorkflowEvent workflowEvent) {
			final int sessionId = processInstance.getSessionHandle().getId();
			workflowEvent.setSessionId(sessionId);
			workflowEvent.setProcessDefinitionId(processInstance.getProcessDefinitionId());
			workflowEvent.setProcessInstanceId(processInstance.getProcessInstanceId());
		}
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		final Private proxy = new CusSoapProxyBuilder(cus).build();
		setEventManager(new WSEventNotifier(proxy, cus));
	}

}
