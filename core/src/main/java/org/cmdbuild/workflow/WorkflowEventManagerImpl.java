package org.cmdbuild.workflow;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.logger.Log;
import org.cmdbuild.workflow.WorkflowPersistence.ProcessCreation;
import org.cmdbuild.workflow.event.WorkflowEvent;
import org.cmdbuild.workflow.event.WorkflowEventManager;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Workflow event manager that uses the legacy persistence layer.
 */
public class WorkflowEventManagerImpl implements WorkflowEventManager {

	private static final Marker marker = MarkerFactory.getMarker(WorkflowEventManagerImpl.class.getName());
	private static final Logger logger = Log.WORKFLOW;

	private static EventMap EMPTY_EVENT_MAP = new EventMap();

	private static class EventMap implements Iterable<WorkflowEvent> {
		private final Map<String, WorkflowEvent> events = new HashMap<String, WorkflowEvent>();

		public void push(final WorkflowEvent event) {
			final String processInstanceId = event.getProcessInstanceId();
			if (!events.containsKey(processInstanceId)) {
				/*
				 * start events must not be overridden by updates, and they
				 * always come first!
				 */
				events.put(processInstanceId, event);
			}
		}

		@Override
		public Iterator<WorkflowEvent> iterator() {
			return events.values().iterator();
		}

	}

	private static class SessionEventMap {
		private final Map<Integer, EventMap> sessionEvents = new HashMap<Integer, EventMap>();

		public void pushEvent(final int sessionId, final WorkflowEvent event) {
			EventMap eventMap = sessionEvents.get(sessionId);
			if (eventMap == null) {
				eventMap = new EventMap();
				sessionEvents.put(sessionId, eventMap);
			}
			eventMap.push(event);
		}

		public Iterable<WorkflowEvent> pullEvents(final int sessionId) {
			final EventMap eventMap = sessionEvents.get(sessionId);
			if (eventMap != null) {
				return eventMap;
			} else {
				return EMPTY_EVENT_MAP;
			}
		}
	}

	private final WorkflowPersistence persistence;
	private final CMWorkflowService service;
	private final WorkflowTypesConverter typesConverter;

	private final SessionEventMap sessionEventMap = new SessionEventMap();

	public WorkflowEventManagerImpl(final WorkflowPersistence persistence, final CMWorkflowService service,
			final WorkflowTypesConverter typesConverter) {
		this.persistence = persistence;
		this.service = service;
		this.typesConverter = typesConverter;
	}

	@Override
	public synchronized void pushEvent(final int sessionId, final WorkflowEvent event) {
		logger.info("pushing event '{}' for session '{}'", //
				ToStringBuilder.reflectionToString(event, ToStringStyle.SHORT_PREFIX_STYLE), //
				sessionId);
		sessionEventMap.pushEvent(sessionId, event);
	}

	@Override
	public synchronized void processEvents(final int sessionId) throws CMWorkflowException {
		logger.info(marker, "processing events for session '{}'", sessionId);
		for (final WorkflowEvent event : sessionEventMap.pullEvents(sessionId)) {
			final WSProcessInstInfo procInstInfo = service.getProcessInstance(event.getProcessInstanceId());
			final CMProcessInstance processInstance = findOrCreateProcessInstance(event, procInstInfo);
			if (processInstance != null) {
				ProcessSynchronizer.of(service, persistence, typesConverter) //
						.syncProcessStateActivitiesAndVariables(processInstance, procInstInfo);
			}
		}
		purgeEvents(sessionId);
	}

	private WSProcessInstInfo fakeClosedProcessInstanceInfo(final WorkflowEvent event) throws CMWorkflowException {
		return new WSProcessInstInfo() {

			@Override
			public String getProcessDefinitionId() {
				return event.getProcessDefinitionId();
			}

			@Override
			public String getPackageId() {
				throw new UnsupportedOperationException("No information");
			}

			@Override
			public String getPackageVersion() {
				throw new UnsupportedOperationException("No information");
			}

			@Override
			public String getProcessInstanceId() {
				return event.getProcessInstanceId();
			}

			@Override
			public WSProcessInstanceState getStatus() {
				return WSProcessInstanceState.COMPLETED;
			}
		};
	}

	private CMProcessInstance findOrCreateProcessInstance(final WorkflowEvent event,
			final WSProcessInstInfo procInstInfo) throws CMWorkflowException {
		switch (event.getType()) {
		case START:
			return persistence.createProcessInstance(procInstInfo, new ProcessCreation() {

				@Override
				public WSProcessInstanceState state() {
					return WSProcessInstanceState.OPEN;
				}

				@Override
				public WSProcessInstInfo processInstanceInfo() {
					return procInstInfo;
				}

			});
		case UPDATE:
			final WSProcessInstInfo info = (procInstInfo == null) ? fakeClosedProcessInstanceInfo(event) : procInstInfo;
			return persistence.findProcessInstance(info);
		default:
			throw new IllegalArgumentException("Invalid event type");
		}
	}

	@Override
	public synchronized void purgeEvents(final int sessionId) {
		sessionEventMap.pullEvents(sessionId);
	}
}
