package org.cmdbuild.workflow;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.api.internal.eventaudit.EventAuditException;
import org.enhydra.shark.api.internal.eventaudit.EventAuditPersistenceObject;
import org.enhydra.shark.api.internal.eventaudit.StateEventAuditPersistenceObject;
import org.enhydra.shark.api.internal.working.CallbackUtilities;

public class DelegatingEventAuditManager extends NullEventAuditManager {

	private static class EventAuditPersistenceObjectWrapper implements SimpleEventManager.ActivityInstance {

		private final WMSessionHandle shandle;
		private final EventAuditPersistenceObject eap;

		private EventAuditPersistenceObjectWrapper(final WMSessionHandle shandle, final EventAuditPersistenceObject eap) {
			this.shandle = shandle;
			this.eap = eap;
		}

		@Override
		public WMSessionHandle getSessionHandle() {
			return shandle;
		}

		@Override
		public String getProcessDefinitionId() {
			return eap.getProcessDefinitionId();
		}

		@Override
		public String getProcessInstanceId() {
			return eap.getProcessId();
		}

		@Override
		public String getActivityDefinitionId() {
			return eap.getActivityDefinitionId();
		}

		@Override
		public String getActivityInstanceId() {
			return eap.getActivityId();
		}

		@Override
		public boolean isNoImplementationActivity() {
			return (eap.getActivityDefinitionType() == XPDLConstants.ACTIVITY_TYPE_NO);
		}
	}

	private enum EventType {
		PROCESS_STATE_CHANGED(SharkConstants.EVENT_PROCESS_STATE_CHANGED), //
		ACTIVITY_STATE_CHANGED(SharkConstants.EVENT_ACTIVITY_STATE_CHANGED), //
		UNKNOWN(null), //
		;

		private final String sharkEventType;

		private EventType(final String sharkEventType) {
			this.sharkEventType = sharkEventType;
		}

		public static EventType fromSharkEventType(final String eventType) {
			if (eventType != null) {
				for (final EventType et : EventType.values()) {
					if (eventType.equals(et.sharkEventType)) {
						return et;
					}
				}
			}
			return UNKNOWN;
		}

	}

	protected enum RunningStates {
		OPEN_RUNNING(SharkConstants.STATE_OPEN_RUNNING), //
		OPEN_NOT_RUNNING_NOT_STARTED(SharkConstants.STATE_OPEN_NOT_RUNNING_NOT_STARTED), //
		OPEN_NOT_RUNNING_SUSPENDED(SharkConstants.STATE_OPEN_NOT_RUNNING_SUSPENDED), //
		CLOSED_COMPLETED(SharkConstants.STATE_CLOSED_COMPLETED), //
		CLOSED_TERMINATED(SharkConstants.STATE_CLOSED_TERMINATED), //
		CLOSED_ABORTED(SharkConstants.STATE_CLOSED_ABORTED), //
		UNKNOWN(StringUtils.EMPTY) //
		;

		private final String sharkRunningState;

		private RunningStates(final String sharkRunningState) {
			this.sharkRunningState = sharkRunningState;
		}

		public boolean isClosed() {
			return sharkRunningState.startsWith(SharkConstants.STATEPREFIX_CLOSED);
		}

		public static RunningStates fromSharkRunningState(final String sharkRunningState) {
			if (sharkRunningState != null) {
				for (final RunningStates runningStates : RunningStates.values()) {
					if (sharkRunningState.equals(runningStates.sharkRunningState)) {
						return runningStates;
					}
				}
			}
			return UNKNOWN;
		}

	}

	private SimpleEventManager eventManager;
	protected CallbackUtilities cus;

	public DelegatingEventAuditManager() {
		setEventManager(new NullEventManager());
	}

	public DelegatingEventAuditManager(final SimpleEventManager eventManager) {
		setEventManager(eventManager);
	}

	public void setEventManager(final SimpleEventManager eventManager) {
		Validate.notNull(eventManager, "Manager cannot be null");
		this.eventManager = eventManager;
	}

	@Override
	public void configure(final CallbackUtilities cus) throws Exception {
		super.configure(cus);
		this.cus = cus;
	}

	@Override
	public void persist(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea)
			throws EventAuditException {
		try {
			final EventType eventType = EventType.fromSharkEventType(sea.getType());
			final RunningStates oldState = RunningStates.fromSharkRunningState(sea.getOldState());
			final RunningStates newState = RunningStates.fromSharkRunningState(sea.getNewState());
			switch (eventType) {
			case PROCESS_STATE_CHANGED:
				fireProcessStateChanged(shandle, sea, oldState, newState);
				break;
			case ACTIVITY_STATE_CHANGED:
				fireActivityStateChanged(shandle, sea, oldState, newState);
				break;
			}
		} catch (final Exception e) {
			throw new EventAuditException(e);
		}
	}

	private void fireProcessStateChanged(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea,
			final RunningStates oldState, final RunningStates newState) {
		if (oldState == RunningStates.OPEN_NOT_RUNNING_NOT_STARTED && newState == RunningStates.OPEN_RUNNING) {
			eventManager.processStarted(processInstanceFor(shandle, sea));
		} else if (newState.isClosed()) {
			eventManager.processClosed(processInstanceFor(shandle, sea));
		} else if (oldState == RunningStates.OPEN_RUNNING && newState == RunningStates.OPEN_NOT_RUNNING_SUSPENDED) {
			eventManager.processSuspended(processInstanceFor(shandle, sea));
		} else if (oldState == RunningStates.OPEN_NOT_RUNNING_SUSPENDED && newState == RunningStates.OPEN_RUNNING) {
			eventManager.processResumed(processInstanceFor(shandle, sea));
		}
	}

	private SimpleEventManager.ProcessInstance processInstanceFor(final WMSessionHandle shandle,
			final EventAuditPersistenceObject eap) {
		return new EventAuditPersistenceObjectWrapper(shandle, eap);
	}

	private void fireActivityStateChanged(final WMSessionHandle shandle, final StateEventAuditPersistenceObject sea,
			final RunningStates oldState, final RunningStates newState) {
		switch (newState) {
		case CLOSED_ABORTED:
		case CLOSED_COMPLETED:
		case CLOSED_TERMINATED:
			eventManager.activityClosed(activityInstanceFor(shandle, sea));
			break;
		case OPEN_NOT_RUNNING_NOT_STARTED:
			if (oldState == RunningStates.UNKNOWN) {
				eventManager.activityStarted(activityInstanceFor(shandle, sea));
			}
			break;
		}
	}

	private SimpleEventManager.ActivityInstance activityInstanceFor(final WMSessionHandle shandle,
			final EventAuditPersistenceObject eap) {
		return new EventAuditPersistenceObjectWrapper(shandle, eap);
	}
}
