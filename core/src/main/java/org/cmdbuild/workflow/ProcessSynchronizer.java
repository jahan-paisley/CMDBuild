package org.cmdbuild.workflow;

import static java.lang.String.format;

import java.util.HashMap;
import java.util.Map;

import org.cmdbuild.dao.entrytype.CMAttribute;
import org.cmdbuild.logger.Log;
import org.cmdbuild.workflow.WorkflowPersistence.ProcessCreation;
import org.cmdbuild.workflow.WorkflowPersistence.ProcessUpdate;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.collect.Maps;

public class ProcessSynchronizer {

	private static final Marker marker = MarkerFactory.getMarker(ProcessSynchronizer.class.getName());
	private static final Logger logger = Log.WORKFLOW;

	public static ProcessSynchronizer of(final CMWorkflowService service, final WorkflowPersistence persistence,
			final WorkflowTypesConverter typesConverter) {
		return new ProcessSynchronizer(service, persistence, typesConverter);
	}

	private final CMWorkflowService workflowService;
	private final WorkflowPersistence persistence;
	private final WorkflowTypesConverter typesConverter;

	private ProcessSynchronizer(final CMWorkflowService service, final WorkflowPersistence persistence,
			final WorkflowTypesConverter typesConverter) {
		this.workflowService = service;
		this.persistence = persistence;
		this.typesConverter = typesConverter;
	}

	public UserProcessInstance syncProcessStateActivitiesAndVariables(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo) throws CMWorkflowException {
		return syncProcessStateActivitiesAndMaybeVariables(processInstance, processInstanceInfo, true);
	}

	public UserProcessInstance syncProcessStateAndActivities(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo) throws CMWorkflowException {
		return syncProcessStateActivitiesAndMaybeVariables(processInstance, processInstanceInfo, false);
	}

	private UserProcessInstance syncProcessStateActivitiesAndMaybeVariables(final CMProcessInstance processInstance,
			final WSProcessInstInfo processInstanceInfo, final boolean syncVariables) throws CMWorkflowException {
		logger.info(marker, "synchronizing process state, activities and (maybe) variables");

		final Map<String, Object> values = Maps.newHashMap();
		if (syncVariables) {
			logger.info(marker, "synchronizing variables");
			final Map<String, Object> workflowValues = workflowService.getProcessInstanceVariables(processInstance
					.getProcessInstanceId());
			final Map<String, Object> nativeValues = fromWorkflowValues(workflowValues);
			for (final CMAttribute a : processInstance.getType().getAttributes()) {
				final String attributeName = a.getName();
				final Object newValue = nativeValues.get(attributeName);
				logger.debug(marker, format("synchronizing variable '%s' with value '%s'", attributeName, newValue));
				values.put(attributeName, newValue);
			}
		}

		final WSProcessInstanceState state;
		final WSActivityInstInfo[] addActivities;
		final WSActivityInstInfo[] activities;
		final WSProcessInstInfo uniqueProcessDefinition;

		if (processInstanceInfo == null) {
			logger.warn(marker,
					"process instance info is null, setting process as completed (should never happen, but who knows...");
			state = WSProcessInstanceState.COMPLETED;
			addActivities = new WSActivityInstInfo[0];
			activities = ProcessUpdate.NO_ACTIVITIES;
			uniqueProcessDefinition = ProcessCreation.NO_PROCESS_INSTANCE_INFO;
		} else {
			uniqueProcessDefinition = processInstanceInfo;
			addActivities = ProcessUpdate.NO_ACTIVITIES;
			activities = workflowService.findOpenActivitiesForProcessInstance(processInstance.getProcessInstanceId());
			state = processInstanceInfo.getStatus();
			if (state == WSProcessInstanceState.COMPLETED) {
				logger.info(marker, "process is completed, delete if from workflow service");
				workflowService.deleteProcessInstance(processInstanceInfo.getProcessInstanceId());
			}
		}

		return persistence.updateProcessInstance(processInstance, new ProcessUpdate() {

			@Override
			public Map<String, ?> values() {
				return values;
			}

			@Override
			public WSProcessInstanceState state() {
				return state;
			}

			@Override
			public WSActivityInstInfo[] addActivities() {
				return addActivities;
			}

			@Override
			public WSActivityInstInfo[] activities() {
				return activities;
			}

			@Override
			public WSProcessInstInfo processInstanceInfo() {
				return uniqueProcessDefinition;
			}

		});
	}

	private final Map<String, Object> fromWorkflowValues(final Map<String, Object> workflowValues) {
		return fromWorkflowValues(workflowValues, typesConverter);
	}

	/*
	 * FIXME AWFUL pre-release hack
	 */
	public static final Map<String, Object> fromWorkflowValues(final Map<String, Object> workflowValues,
			final WorkflowTypesConverter workflowVariableConverter) {
		final Map<String, Object> nativeValues = new HashMap<String, Object>();
		for (final Map.Entry<String, Object> wv : workflowValues.entrySet()) {
			nativeValues.put(wv.getKey(), workflowVariableConverter.fromWorkflowType(wv.getValue()));
		}
		return nativeValues;
	}

}
