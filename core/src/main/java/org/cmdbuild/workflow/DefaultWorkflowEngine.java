package org.cmdbuild.workflow;

import static java.util.Arrays.asList;

import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.auth.AuthenticationService;
import org.cmdbuild.auth.acl.CMGroup;
import org.cmdbuild.auth.acl.NullGroup;
import org.cmdbuild.auth.user.AuthenticatedUser;
import org.cmdbuild.auth.user.OperationUser;
import org.cmdbuild.common.Builder;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.cmdbuild.logger.Log;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.workflow.WorkflowPersistence.ProcessCreation;
import org.cmdbuild.workflow.WorkflowPersistence.ProcessUpdate;
import org.cmdbuild.workflow.WorkflowTypesConverter.Reference;
import org.cmdbuild.workflow.service.CMWorkflowService;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.slf4j.Logger;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

/**
 * Mediator between the {@link WorkflowPersistence} and the
 * {@link CMWorkflowService}.
 */
public class DefaultWorkflowEngine implements QueryableUserWorkflowEngine {

	private static final Marker marker = MarkerFactory.getMarker(DefaultWorkflowEngine.class.getName());
	private static final Logger logger = Log.WORKFLOW;

	public static class DefaultWorkflowEngineBuilder implements Builder<DefaultWorkflowEngine> {

		private OperationUser operationUser;
		private WorkflowPersistence persistence;
		private CMWorkflowService service;
		private WorkflowTypesConverter typesConverter;
		private CMWorkflowEngineListener engineListener = NULL_EVENT_LISTENER;
		private AuthenticationService authenticationService;

		@Override
		public DefaultWorkflowEngine build() {
			return new DefaultWorkflowEngine(this);
		}

		public DefaultWorkflowEngineBuilder withOperationUser(final OperationUser value) {
			this.operationUser = value;
			return this;
		}

		public void setOperationUser(final OperationUser operationUser) {
			this.operationUser = operationUser;
		}

		public DefaultWorkflowEngineBuilder withPersistence(final WorkflowPersistence value) {
			this.persistence = value;
			return this;
		}

		public void setPersistence(final WorkflowPersistence persistence) {
			this.persistence = persistence;
		}

		public DefaultWorkflowEngineBuilder withService(final CMWorkflowService value) {
			this.service = value;
			return this;
		}

		public void setService(final CMWorkflowService service) {
			this.service = service;
		}

		public DefaultWorkflowEngineBuilder withTypesConverter(final WorkflowTypesConverter value) {
			this.typesConverter = value;
			return this;
		}

		public void setTypesConverter(final WorkflowTypesConverter typesConverter) {
			this.typesConverter = typesConverter;
		}

		public DefaultWorkflowEngineBuilder withEventListener(final CMWorkflowEngineListener value) {
			this.engineListener = value;
			return this;
		}

		public void setEventListener(final CMWorkflowEngineListener engineListener) {
			this.engineListener = engineListener;
		}

		public DefaultWorkflowEngineBuilder withAuthenticationService(final AuthenticationService value) {
			this.authenticationService = value;
			return this;
		}

		public void setAuthenticationService(final AuthenticationService authenticationService) {
			this.authenticationService = authenticationService;
		}

	}

	public static DefaultWorkflowEngineBuilder newInstance() {
		return new DefaultWorkflowEngineBuilder();
	}

	private static final CMWorkflowEngineListener NULL_EVENT_LISTENER = new NullWorkflowEngineListener();

	private final OperationUser operationUser;
	private final WorkflowPersistence persistence;
	private final CMWorkflowService service;
	private final WorkflowTypesConverter typesConverter;
	private CMWorkflowEngineListener eventListener;
	private final AuthenticationService authenticationService;

	private DefaultWorkflowEngine(final DefaultWorkflowEngineBuilder builder) {
		this.operationUser = builder.operationUser;
		this.persistence = builder.persistence;
		this.service = builder.service;
		this.typesConverter = builder.typesConverter;
		this.eventListener = builder.engineListener;
		this.authenticationService = builder.authenticationService;
	}

	@Override
	public void setEventListener(final CMWorkflowEngineListener eventListener) {
		this.eventListener = eventListener;
	}

	@Override
	public UserProcessClass findProcessClassById(final Long id) {
		logger.info(marker, "getting process class with id '{}'", id);
		Validate.notNull(id);
		return persistence.findProcessClass(id);
	}

	@Override
	public UserProcessClass findProcessClassByName(final String name) {
		logger.info(marker, "getting process class with name '{}'", name);
		Validate.notNull(name);
		return persistence.findProcessClass(name);
	}

	@Override
	public Iterable<UserProcessClass> findProcessClasses() {
		logger.info(marker, "getting all active process classes");
		return Iterables.filter(findAllProcessClasses(), new Predicate<UserProcessClass>() {

			@Override
			public boolean apply(final UserProcessClass input) {
				return input.isActive();
			}

		});
	}

	@Override
	public Iterable<UserProcessClass> findAllProcessClasses() {
		logger.info(marker, "getting all process classes");
		return persistence.getAllProcessClasses();
	}

	@Override
	public UserProcessInstance startProcess(final CMProcessClass processClass) throws CMWorkflowException {
		logger.info(marker, "starting process for class '{}'", processClass.getName());

		final CMActivity startActivity = processClass.getStartActivity();
		if (startActivity == null) {
			return null;
		}
		final WSProcessInstInfo procInstInfo = service.startProcess(processClass.getPackageId(),
				processClass.getProcessDefinitionId());
		final WSActivityInstInfo startActInstInfo = keepOnlyStartingActivityInstance(startActivity.getId(),
				procInstInfo.getProcessInstanceId());

		final UserProcessInstance createdProcessInstance = persistence.createProcessInstance(processClass,
				procInstInfo, new ProcessCreation() {

					@Override
					public WSProcessInstInfo processInstanceInfo() {
						return procInstInfo;
					}

					@Override
					public WSProcessInstanceState state() {
						return WSProcessInstanceState.OPEN;
					}

				});
		final UserProcessInstance processInstance = persistence.updateProcessInstance(createdProcessInstance,
				new ProcessUpdate() {

					@Override
					public Map<String, ?> values() {
						return NO_VALUES;
					}

					@Override
					public WSProcessInstInfo processInstanceInfo() {
						return NO_PROCESS_INSTANCE_INFO;
					}

					@Override
					public WSProcessInstanceState state() {
						return NO_STATE;
					}

					@Override
					public WSActivityInstInfo[] addActivities() {
						return new WSActivityInstInfo[] { activityWithSpecificParticipant( //
								startActInstInfo, //
								operationUser.getPreferredGroup().getName()) };
					}

					@Override
					public WSActivityInstInfo[] activities() {
						return NO_ACTIVITIES;
					}

				});
		fillCardInfoAndProcessInstanceIdOnProcessInstance(processInstance);
		return persistence.findProcessInstance(createdProcessInstance);
	}

	private WSActivityInstInfo activityWithSpecificParticipant(final WSActivityInstInfo wsActivityInstInfo,
			final String participant) {
		return new WSActivityInstInfo() {

			@Override
			public String getProcessInstanceId() {
				return wsActivityInstInfo.getProcessInstanceId();
			}

			@Override
			public String[] getParticipants() {
				return new String[] { participant };
			}

			@Override
			public String getActivityName() {
				return wsActivityInstInfo.getActivityName();
			}

			@Override
			public String getActivityInstanceId() {
				return wsActivityInstInfo.getActivityInstanceId();
			}

			@Override
			public String getActivityDescription() {
				return wsActivityInstInfo.getActivityDescription();
			}

			@Override
			public String getActivityDefinitionId() {
				return wsActivityInstInfo.getActivityDefinitionId();
			}

		};
	}

	private WSActivityInstInfo keepOnlyStartingActivityInstance(final String startActivityId,
			final String processInstanceId) throws CMWorkflowException {
		WSActivityInstInfo startActivityInstanceInfo = null;
		final Iterable<WSActivityInstInfo> activityInstanceInfos = asList(service
				.findOpenActivitiesForProcessInstance(processInstanceId));
		for (final WSActivityInstInfo activityInstanceInfo : activityInstanceInfos) {
			final String activityDefinitionId = activityInstanceInfo.getActivityDefinitionId();
			if (startActivityId.equals(activityDefinitionId)) {
				startActivityInstanceInfo = activityInstanceInfo;
			} else {
				final String activityInstanceId = activityInstanceInfo.getActivityInstanceId();
				service.abortActivityInstance(processInstanceId, activityInstanceId);
			}
		}
		return startActivityInstanceInfo;
	}

	private void fillCardInfoAndProcessInstanceIdOnProcessInstance(final UserProcessInstance procInst)
			throws CMWorkflowException {
		final String procInstId = procInst.getProcessInstanceId();
		final Map<String, Object> extraVars = Maps.newHashMap();
		extraVars.put(Constants.PROCESS_CARD_ID_VARIABLE, procInst.getCardId());
		extraVars.put(Constants.PROCESS_CLASSNAME_VARIABLE, procInst.getType().getName());
		extraVars.put(Constants.PROCESS_INSTANCE_ID_VARIABLE, procInstId);
		service.setProcessInstanceVariables(procInstId, toWorkflowValues(procInst.getType(), extraVars));
	}

	private final Map<String, Object> toWorkflowValues(final CMProcessClass processClass,
			final Map<String, Object> nativeValues) {
		final Map<String, Object> workflowValues = Maps.newHashMap();
		for (final Map.Entry<String, Object> nv : nativeValues.entrySet()) {
			final String attributeName = nv.getKey();
			CMAttributeType<?> attributeType;
			try {
				attributeType = processClass.getAttribute(attributeName).getType();
			} catch (final Exception e) {
				attributeType = null;
			}
			workflowValues.put(attributeName, typesConverter.toWorkflowType(attributeType, nv.getValue()));
		}
		return workflowValues;
	}

	@Override
	public void abortProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		logger.info(marker, "aborting process instance for class '{}' and id '{}'", //
				processInstance.getType().getName(), processInstance.getCardId());
		service.abortProcessInstance(processInstance.getProcessInstanceId());
	}

	@Override
	public void suspendProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		logger.info(marker, "suspending process instance for class '{}' and id '{}'", //
				processInstance.getType().getName(), processInstance.getCardId());
		service.suspendProcessInstance(processInstance.getProcessInstanceId());
	}

	@Override
	public void resumeProcessInstance(final CMProcessInstance processInstance) throws CMWorkflowException {
		logger.info(marker, "resuming process instance for class '{}' and id '{}'", //
				processInstance.getType().getName(), processInstance.getCardId());
		service.resumeProcessInstance(processInstance.getProcessInstanceId());
	}

	@Override
	public void updateActivity(final CMActivityInstance activityInstance, final Map<String, ?> inputValues,
			final Map<String, Object> widgetSubmission) throws CMWorkflowException {
		logger.info(marker, "updating activity instance '{}' for process '{}'", //
				activityInstance.getId(), activityInstance.getProcessInstance().getType().getName());

		final CMProcessInstance processInstance = activityInstance.getProcessInstance();
		persistence.updateProcessInstance(processInstance, new ProcessUpdate() {

			@Override
			public Map<String, ?> values() {
				return inputValues;
			}

			@Override
			public WSProcessInstanceState state() {
				return NO_STATE;
			}

			@Override
			public WSProcessInstInfo processInstanceInfo() {
				return NO_PROCESS_INSTANCE_INFO;
			}

			@Override
			public WSActivityInstInfo[] addActivities() {
				return NO_ACTIVITIES;
			}

			@Override
			public WSActivityInstInfo[] activities() {
				return NO_ACTIVITIES;
			}

		});

		final Map<String, Object> nativeValues = Maps.newHashMap(inputValues);
		nativeValues.put(Constants.CURRENT_USER_USERNAME_VARIABLE, currentUserUsername());
		nativeValues.put(Constants.CURRENT_GROUP_NAME_VARIABLE, currentGroupName());
		nativeValues.put(Constants.CURRENT_USER_VARIABLE, currentUserReference());
		nativeValues.put(Constants.CURRENT_GROUP_VARIABLE, currentGroupReference(activityInstance));

		saveWidgets(activityInstance, widgetSubmission, nativeValues);
		service.setProcessInstanceVariables(processInstance.getProcessInstanceId(),
				toWorkflowValues(processInstance.getType(), nativeValues));
	}

	private Reference currentUserReference() {
		final AuthenticatedUser authenticatedUser = operationUser.getAuthenticatedUser();
		return new Reference() {

			@Override
			public Long getId() {
				return authenticatedUser.getId();
			}

			@Override
			public String getClassName() {
				return "User";
			}

		};
	}

	private String currentUserUsername() {
		return operationUser.getAuthenticatedUser().getUsername();
	}

	private String currentGroupName() {
		return operationUser.getPreferredGroup().getName();
	}

	private Reference currentGroupReference(final CMActivityInstance activityInstance) {
		final CMGroup group = authenticationService.fetchGroupWithName(activityInstance.getPerformerName());
		if (group instanceof NullGroup) {
			return null;
		} else {
			return new Reference() {

				@Override
				public Long getId() {
					return Long.valueOf(group.getId());
				}

				@Override
				public String getClassName() {
					return "Role";
				}

			};
		}
	}

	private void saveWidgets(final CMActivityInstance activityInstance, final Map<String, Object> widgetSubmission,
			final Map<String, Object> nativeValues) throws CMWorkflowException {
		for (final CMActivityWidget w : activityInstance.getWidgets()) {
			final Object submission = widgetSubmission.get(w.getStringId());
			if (submission == null) {
				continue;
			}
			try {
				w.save(activityInstance, submission, nativeValues);
			} catch (final Exception e) {
				throw new CMWorkflowException("Widget save failed", e);
			}
		}
	}

	@Override
	public UserProcessInstance advanceActivity(final CMActivityInstance activityInstance) throws CMWorkflowException {
		logger.info(marker, "advancing activity instance '{}' for process '{}'", //
				activityInstance.getId(), activityInstance.getProcessInstance().getType().getName());

		final CMProcessInstance procInst = activityInstance.getProcessInstance();
		final String processInstanceId = procInst.getProcessInstanceId();
		for (final CMActivityWidget activityWidget : activityInstance.getWidgets()) {
			activityWidget.advance(activityInstance);
		}
		service.advanceActivityInstance(processInstanceId, activityInstance.getId());
		return persistence.findProcessInstance(procInst);
	}

	/**
	 * It should extract CMProcessClass with findAllProcessClasses() but the new
	 * DAO is not here yet. If it wasn't for SQL, we would breathe hacks.
	 * 
	 * @throws CMWorkflowException
	 */
	@Override
	public void sync() throws CMWorkflowException {
		eventListener.syncStarted();
		for (final UserProcessClass processClass : persistence.getAllProcessClasses()) {
			if (processClass.isSuperclass()) {
				continue;
			}
			syncProcess(processClass);
		}
		eventListener.syncFinished();
	}

	private void syncProcess(final UserProcessClass processClass) throws CMWorkflowException {
		eventListener.syncProcessStarted(processClass);
		final Map<String, WSProcessInstInfo> wsInfo = queryWSOpenAndSuspended(processClass);
		final Iterable<? extends CMProcessInstance> activeProcessInstances = persistence
				.queryOpenAndSuspended(processClass);
		for (final CMProcessInstance processInstance : activeProcessInstances) {
			final String processInstanceId = processInstance.getProcessInstanceId();
			final WSProcessInstInfo processInstanceInfo = wsInfo.get(processInstanceId);
			if (processInstanceInfo == null) {
				eventListener.syncProcessInstanceNotFound(processInstance);
				removeOutOfSyncProcess(processInstance);
			} else {
				eventListener.syncProcessInstanceFound(processInstance);
				ProcessSynchronizer.of(service, persistence, typesConverter) //
						.syncProcessStateAndActivities(processInstance, processInstanceInfo);
			}
		}
	}

	private Map<String, WSProcessInstInfo> queryWSOpenAndSuspended(final CMProcessClass processClass)
			throws CMWorkflowException {
		final Map<String, WSProcessInstInfo> wsInfo = Maps.newHashMap();
		final String processDefinitionId = processClass.getProcessDefinitionId();
		if (processDefinitionId != null) {
			for (final WSProcessInstInfo pis : service.listOpenProcessInstances(processDefinitionId)) {
				wsInfo.put(pis.getProcessInstanceId(), pis);
			}
		}
		return wsInfo;
	}

	@Override
	public PagedElements<UserProcessInstance> query(final String className, final QueryOptions queryOptions) {
		return persistence.query(className, queryOptions);
	}

	private void removeOutOfSyncProcess(final CMProcessInstance processInstance) throws CMWorkflowException {
		persistence.updateProcessInstance(processInstance, new ProcessUpdate() {

			@Override
			public Map<String, ?> values() {
				return NO_VALUES;
			}

			@Override
			public WSProcessInstanceState state() {
				return WSProcessInstanceState.ABORTED;
			}

			@Override
			public WSProcessInstInfo processInstanceInfo() {
				return NO_PROCESS_INSTANCE_INFO;
			}

			@Override
			public WSActivityInstInfo[] addActivities() {
				return NO_ACTIVITIES;
			}

			@Override
			public WSActivityInstInfo[] activities() {
				return NO_ACTIVITIES;
			}

		});
	}

	@Override
	public UserProcessInstance findProcessInstance(final CMProcessClass processDefinition, final Long cardId) {
		return persistence.findProcessInstance(processDefinition, cardId);
	}

}
