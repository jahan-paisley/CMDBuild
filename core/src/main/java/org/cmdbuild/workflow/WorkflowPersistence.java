package org.cmdbuild.workflow;

import java.util.Map;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.workflow.service.WSActivityInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;
import org.cmdbuild.workflow.user.UserProcessClass;
import org.cmdbuild.workflow.user.UserProcessInstance;

public interface WorkflowPersistence {

	interface ProcessCreation {

		WSProcessInstanceState NO_STATE = null;
		WSProcessInstInfo NO_PROCESS_INSTANCE_INFO = null;

		WSProcessInstanceState state();

		WSProcessInstInfo processInstanceInfo();

	}

	interface ProcessUpdate extends ProcessCreation {

		Map<String, ?> NO_VALUES = null;
		WSActivityInstInfo[] NO_ACTIVITIES = null;

		Map<String, ?> values();

		WSActivityInstInfo[] addActivities();

		WSActivityInstInfo[] activities();

	}

	Iterable<UserProcessClass> getAllProcessClasses();

	UserProcessClass findProcessClass(Long id);

	UserProcessClass findProcessClass(String name);

	UserProcessInstance createProcessInstance(WSProcessInstInfo processInstInfo, ProcessCreation processCreation)
			throws CMWorkflowException;

	UserProcessInstance createProcessInstance(CMProcessClass processClass, WSProcessInstInfo processInstInfo,
			ProcessCreation processCreation) throws CMWorkflowException;

	UserProcessInstance updateProcessInstance(CMProcessInstance processInstance, ProcessUpdate processUpdate)
			throws CMWorkflowException;

	UserProcessInstance findProcessInstance(WSProcessInstInfo processInstInfo) throws CMWorkflowException;

	UserProcessInstance findProcessInstance(CMProcessInstance processInstance);

	UserProcessInstance findProcessInstance(CMProcessClass processClass, Long cardId);

	Iterable<? extends UserProcessInstance> queryOpenAndSuspended(UserProcessClass processClass);

	PagedElements<UserProcessInstance> query(String className, QueryOptions queryOptions);

}
