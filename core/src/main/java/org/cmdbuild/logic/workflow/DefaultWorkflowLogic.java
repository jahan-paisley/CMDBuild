package org.cmdbuild.logic.workflow;

import static com.google.common.collect.FluentIterable.from;
import static java.lang.String.format;
import static org.cmdbuild.logic.PrivilegeUtils.assure;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.DataSource;

import org.cmdbuild.auth.acl.PrivilegeContext;
import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.config.WorkflowConfiguration;
import org.cmdbuild.dao.entrytype.CMClass;
import org.cmdbuild.dao.view.CMDataView;
import org.cmdbuild.data.store.lookup.LookupStore;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.exception.ConsistencyException.ConsistencyExceptionType;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.logic.data.access.ProcessEntryFiller;
import org.cmdbuild.logic.data.access.resolver.ForeignReferenceResolver;
import org.cmdbuild.logic.data.access.resolver.ReferenceAndLookupSerializer;
import org.cmdbuild.services.FilesStore;
import org.cmdbuild.workflow.CMActivity;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMProcessInstance;
import org.cmdbuild.workflow.CMWorkflowException;
import org.cmdbuild.workflow.QueryableUserWorkflowEngine;
import org.cmdbuild.workflow.user.UserActivityInstance;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.joda.time.DateTime;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;

class DefaultWorkflowLogic implements WorkflowLogic {

	private static final UserActivityInstance NULL_ACTIVITY_INSTANCE = null;

	private static final String BEGIN_DATE_ATTRIBUTE = "beginDate";
	private static final String SKETCH_PATH = "images" + File.separator + "workflow" + File.separator;

	private final PrivilegeContext privilegeContext;
	private final QueryableUserWorkflowEngine workflowEngine;
	private final CMDataView dataView;
	private final CMDataView systemDataView;
	private final LookupStore lookupStore;
	private final WorkflowConfiguration configuration;
	private final FilesStore filesStore;

	public DefaultWorkflowLogic( //
			final PrivilegeContext privilegeContext, //
			final QueryableUserWorkflowEngine workflowEngine, //
			final CMDataView dataView, //
			final CMDataView systemDataView, //
			final LookupStore lookupStore, //
			final WorkflowConfiguration configuration, //
			final FilesStore filesStore //
	) {
		this.privilegeContext = privilegeContext;
		this.workflowEngine = workflowEngine;
		this.dataView = dataView;
		this.systemDataView = systemDataView;
		this.lookupStore = lookupStore;
		this.configuration = configuration;
		this.filesStore = filesStore;
	}

	/*
	 * Ungliness to be used in old code
	 */

	@Override
	public boolean isProcessUsable(final String className) {
		return isWorkflowEnabled() && workflowEngine.findProcessClassByName(className).isUsable();
	}

	@Override
	public boolean isWorkflowEnabled() {
		return configuration.isEnabled();
	}

	@Override
	public PagedElements<UserProcessInstance> query(final String className, final QueryOptions queryOptions) {
		final PagedElements<UserProcessInstance> fetchedProcesses = workflowEngine.query(className, queryOptions);
		final CMClass processClass = dataView.findClass(className);
		final Iterable<UserProcessInstance> processes = ForeignReferenceResolver.<UserProcessInstance> newInstance() //
				.withSystemDataView(systemDataView) //
				.withEntryType(processClass) //
				.withEntries(fetchedProcesses) //
				.withEntryFiller(new ProcessEntryFiller()) //
				.withLookupStore(lookupStore) //
				.withSerializer(new ReferenceAndLookupSerializer<UserProcessInstance>()) //
				.build() //
				.resolve();
		return new PagedElements<UserProcessInstance>(processes, fetchedProcesses.totalSize());
	}

	@Override
	public Iterable<UserProcessClass> findAllProcessClasses() {
		return workflowEngine.findAllProcessClasses();
	}

	@Override
	public Iterable<? extends UserProcessClass> findActiveProcessClasses() {
		final Iterable<UserProcessClass> allClasses;
		if (configuration.isEnabled()) {
			allClasses = workflowEngine.findProcessClasses();
		} else {
			allClasses = Collections.emptyList();
		}
		return allClasses;
	}

	@Override
	public UserProcessClass findProcessClass(final String className) {
		final Optional<UserProcessClass> optional = from(findAllProcessClasses()) //
				.filter(new Predicate<UserProcessClass>() {
					@Override
					public boolean apply(final UserProcessClass input) {
						return input.getName().equals(className);
					}
				}).first();
		return optional.isPresent() ? optional.get() : null;
	}

	/*
	 * Management
	 */

	/**
	 * Returns the process start activity for the current user.
	 * 
	 * @param process
	 *            class name
	 * @return the start activity definition
	 * @throws CMWorkflowException
	 */
	@Override
	public CMActivity getStartActivity(final String processClassName) throws CMWorkflowException {
		return workflowEngine.findProcessClassByName(processClassName).getStartActivity();
	}

	@Override
	public CMActivity getStartActivityOrDie( //
			final String processClassName //
	) throws CMWorkflowException, CMDBWorkflowException {

		final UserProcessClass theProess = workflowEngine.findProcessClassByName(processClassName);
		final CMActivity theActivity = theProess.getStartActivity();
		if (theActivity == null) {
			throw WorkflowExceptionType.WF_START_ACTIVITY_NOT_FOUND.createException(theProess.getDescription());
		}

		return theActivity;
	}

	@Override
	public CMActivity getStartActivityOrDie( //
			final Long processClassId //
	) throws CMWorkflowException, CMDBWorkflowException {

		final UserProcessClass theProess = workflowEngine.findProcessClassById(processClassId);
		final CMActivity theActivity = theProess.getStartActivity();
		if (theActivity == null) {
			throw WorkflowExceptionType.WF_START_ACTIVITY_NOT_FOUND.createException(theProess.getDescription());
		}

		return theActivity;
	}

	/**
	 * Returns the process start activity for the current user.
	 * 
	 * @param process
	 *            class id
	 * @return the start activity definition
	 * @throws CMWorkflowException
	 */
	@Override
	public CMActivity getStartActivity(final Long processClassId) throws CMWorkflowException {
		logger.debug("getting starting activity for process with class id '{}'", processClassId);
		return workflowEngine.findProcessClassById(processClassId).getStartActivity();
	}

	@Override
	public UserProcessInstance getProcessInstance(final String processClassName, final Long cardId) {
		logger.debug("getting process instance for class name '{}' and card id '{}'", processClassName, cardId);
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		return workflowEngine.findProcessInstance(processClass, cardId);
	}

	@Override
	public UserProcessInstance getProcessInstance(final Long processClassId, final Long cardId) {
		logger.debug("getting process instance for class id '{}' and card id '{}'", processClassId, cardId);
		final CMProcessClass processClass = workflowEngine.findProcessClassById(processClassId);
		return workflowEngine.findProcessInstance(processClass, cardId);
	}

	@Override
	public UserActivityInstance getActivityInstance(final String processClassName, final Long processCardId,
			final String activityInstanceId) {
		logger.debug("getting activity instance '{}' for process '{}'", activityInstanceId, processClassName);
		final UserProcessInstance processInstance = getProcessInstance(processClassName, processCardId);
		return getActivityInstance(processInstance, activityInstanceId);
	}

	@Override
	public UserActivityInstance getActivityInstance(final Long processClassId, final Long processCardId,
			final String activityInstanceId) {
		logger.debug("getting activity instance '{}' for process '{}'", activityInstanceId, processClassId);
		final UserProcessInstance processInstance = getProcessInstance(processClassId, processCardId);
		return getActivityInstance(processInstance, activityInstanceId);
	}

	private UserActivityInstance getActivityInstance(final UserProcessInstance processInstance,
			final String activityInstanceId) {
		for (final UserActivityInstance activityInstance : processInstance.getActivities()) {
			if (activityInstance.getId().equals(activityInstanceId)) {
				return activityInstance;
			}
		}
		logger.error("activity instance '{}' not found", activityInstanceId);
		return NULL_ACTIVITY_INSTANCE;
	}

	/**
	 * Retrieve the processInstance and check if the given date is the same of
	 * the process begin date in this case, we assume that the process is
	 * updated
	 * 
	 * @param processClassName
	 * @param processInstanceId
	 * @param givenBeginDate
	 * @return
	 */
	@Override
	public boolean isProcessUpdated( //
			final String processClassName, //
			final Long processInstanceId, //
			final DateTime givenBeginDate //
	) {

		final CMProcessInstance processInstance = getProcessInstance(processClassName, processInstanceId);
		return isProcessUpdated(processInstance, givenBeginDate);
	}

	private boolean isProcessUpdated(final CMProcessInstance processInstance, final DateTime givenBeginDate) {
		final DateTime currentBeginDate = processInstance.getBeginDate();
		return givenBeginDate.equals(currentBeginDate);
	}

	/**
	 * Starts the process, kills every activity except for the one that this
	 * user wanted to start, advances it if requested.
	 * 
	 * @param processClassName
	 *            process class name
	 * @param vars
	 *            values
	 * @param widgetSubmission
	 * @param advance
	 * 
	 * @return the created process instance
	 * 
	 * @throws CMWorkflowException
	 */
	@Override
	public UserProcessInstance startProcess(final String processClassName, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		return startProcess(processClass, vars, widgetSubmission, advance);
	}

	/**
	 * Starts the process, kills every activity except for the one that this
	 * user wanted to start, advances it if requested.
	 * 
	 * @param processClassId
	 *            process class id
	 * @param vars
	 *            values
	 * @param widgetSubmission
	 * @param advance
	 * 
	 * @return the created process instance
	 * 
	 * @throws CMWorkflowException
	 */
	@Override
	public UserProcessInstance startProcess(final Long processClassId, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final CMProcessClass proc = workflowEngine.findProcessClassById(processClassId);
		return startProcess(proc, vars, widgetSubmission, advance);
	}

	private UserProcessInstance startProcess(final CMProcessClass process, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final UserProcessInstance procInst = workflowEngine.startProcess(process);
		final Map<String, Object> mergedVars = mergeVars(procInst.getValues(), vars);
		return updateOnlyActivity(procInst, mergedVars, widgetSubmission, advance);
	}

	/**
	 * This awful hack is needed because SOMEONE decided that it was a good idea
	 * to specify default attributes in the database, so old clients did it and
	 * now we have to deal with it.
	 * 
	 * @param databaseValues
	 *            values as they are in the newly created database row
	 * @param entrySet
	 *            values submitted in the form
	 * @return database values overridden by the submitted ones
	 */
	private Map<String, Object> mergeVars(final Iterable<Entry<String, Object>> databaseValues,
			final Map<String, ?> submittedValues) {
		final Map<String, Object> mergedValues = new HashMap<String, Object>();
		for (final Entry<String, ?> e : databaseValues) {
			mergedValues.put(e.getKey(), e.getValue());
		}
		for (final Entry<String, ?> e : submittedValues.entrySet()) {
			mergedValues.put(e.getKey(), e.getValue());
		}
		return mergedValues;
	}

	@Override
	public UserProcessInstance updateProcess(final String processClassName, final Long processCardId,
			final String activityInstanceId, final Map<String, ?> vars, final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		final UserProcessInstance processInstance = workflowEngine.findProcessInstance(processClass, processCardId);
		return updateProcess( //
				processInstance, //
				activityInstanceId, //
				vars, //
				widgetSubmission, //
				advance);
	}

	@Override
	public UserProcessInstance updateProcess(final Long processClassId, final Long processCardId,
			final String activityInstanceId, final Map<String, ?> vars, final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {

		final CMProcessClass processClass = workflowEngine.findProcessClassById(processClassId);
		final UserProcessInstance processInstance = workflowEngine.findProcessInstance(processClass, processCardId);

		// check if the given begin date is the same
		// of the stored process, to be sure to deny
		// the update of old versions
		if (vars.containsKey(BEGIN_DATE_ATTRIBUTE)) {
			final Long givenBeginDateAsLong = (Long) vars.get(BEGIN_DATE_ATTRIBUTE);
			final DateTime givenBeginDate = new DateTime(givenBeginDateAsLong);
			if (!isProcessUpdated(processInstance, givenBeginDate)) {
				throw ConsistencyExceptionType.OUT_OF_DATE_PROCESS.createException();
			}

			// must be removed to not use it
			// as a custom attribute
			vars.remove(BEGIN_DATE_ATTRIBUTE);
		}

		updateProcess( //
				processInstance, //
				activityInstanceId, //
				vars, //
				widgetSubmission, //
				advance);

		// retrieve again the processInstance because the updateProcess return
		// the
		// old processInstance, not the updated.
		return workflowEngine.findProcessInstance(processClass, processCardId);
	}

	private UserProcessInstance updateProcess(final UserProcessInstance processInstance,
			final String activityInstanceId, final Map<String, ?> vars, final Map<String, Object> widgetSubmission,
			final boolean advance) throws CMWorkflowException {
		final UserActivityInstance activityInstance = processInstance.getActivityInstance(activityInstanceId);
		return updateActivity(activityInstance, vars, widgetSubmission, advance);
	}

	/**
	 * Updates and (optionally) advances the only activity of a process
	 * instance.
	 * 
	 * @param procInst
	 *            process instance
	 * @param vars
	 *            variables to update
	 * @param advance
	 * @return the updated process instance
	 * @throws CMWorkflowException
	 */
	private UserProcessInstance updateOnlyActivity(final UserProcessInstance procInst, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		final List<UserActivityInstance> activities = procInst.getActivities();
		if (activities.size() != 1) {
			throw new UnsupportedOperationException(format("Not just one activity to advance! (%d activities)",
					activities.size()));
		}
		final UserActivityInstance firstActInst = activities.get(0);
		return updateActivity(firstActInst, vars, widgetSubmission, advance);
	}

	private UserProcessInstance updateActivity(final UserActivityInstance activityInstance, final Map<String, ?> vars,
			final Map<String, Object> widgetSubmission, final boolean advance) throws CMWorkflowException {
		workflowEngine.updateActivity(activityInstance, vars, widgetSubmission);
		if (advance) {
			return workflowEngine.advanceActivity(activityInstance);
		} else {
			return activityInstance.getProcessInstance();
		}
	}

	@Override
	public void suspendProcess(final String processClassName, final Long processCardId) throws CMWorkflowException {
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		final UserProcessInstance processInstance = workflowEngine.findProcessInstance(processClass, processCardId);
		workflowEngine.suspendProcessInstance(processInstance);
	}

	@Override
	public void resumeProcess(final String processClassName, final Long processCardId) throws CMWorkflowException {
		final CMProcessClass processClass = workflowEngine.findProcessClassByName(processClassName);
		final UserProcessInstance processInstance = workflowEngine.findProcessInstance(processClass, processCardId);
		workflowEngine.resumeProcessInstance(processInstance);
	}

	/*
	 * Administration
	 */

	@Override
	public void sync() throws CMWorkflowException {
		assure(privilegeContext.hasAdministratorPrivileges());
		workflowEngine.sync();
	}

	@Override
	public DataSource getProcessDefinitionTemplate(final Long processClassId) throws CMWorkflowException {
		return workflowEngine.findProcessClassById(processClassId).getDefinitionTemplate();
	}

	@Override
	public String[] getProcessDefinitionVersions(final Long processClassId) throws CMWorkflowException {
		return workflowEngine.findProcessClassById(processClassId).getDefinitionVersions();
	}

	@Override
	public DataSource getProcessDefinition(final Long processClassId, final String version) throws CMWorkflowException {
		return workflowEngine.findProcessClassById(processClassId).getDefinition(version);
	}

	@Override
	public void updateProcessDefinition(final Long processClassId, final DataSource xpdlFile)
			throws CMWorkflowException {
		workflowEngine.findProcessClassById(processClassId).updateDefinition(xpdlFile);
	}

	/*
	 * It's WRONG to display the latest sketch for every process
	 */

	@Override
	public void removeSketch(final Long processClassId) {
		final CMProcessClass process = workflowEngine.findProcessClassById(processClassId);
		final String filterPattern = process.getName() + ".*";
		final String[] processImages = filesStore.list(SKETCH_PATH, filterPattern);
		if (processImages.length > 0) {
			filesStore.remove(SKETCH_PATH + processImages[0]);
		}
	}

	@Override
	public void addSketch(final Long processClassId, final DataSource ds) throws IOException {
		final CMProcessClass process = workflowEngine.findProcessClassById(processClassId);
		final String relativeUploadPath = SKETCH_PATH + process.getName() + filesStore.getExtension(ds.getName());
		filesStore.save(ds.getInputStream(), relativeUploadPath);
	}

	@Override
	public void abortProcess(final Long processClassId, final long processCardId) throws CMWorkflowException {
		logger.info("aborting process with id '{}' for class '{}'", processCardId, processClassId);
		if (processCardId < 0) {
			logger.error("invalid card id '{}'", processCardId);
			throw WorkflowExceptionType.WF_CANNOT_ABORT_PROCESS.createException();
		}
		final CMProcessClass process = workflowEngine.findProcessClassById(processClassId);
		final UserProcessInstance pi = workflowEngine.findProcessInstance(process, processCardId);
		workflowEngine.abortProcessInstance(pi);
	}

}
