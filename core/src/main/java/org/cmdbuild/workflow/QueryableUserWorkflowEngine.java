package org.cmdbuild.workflow;

import org.cmdbuild.common.utils.PagedElements;
import org.cmdbuild.logic.data.QueryOptions;
import org.cmdbuild.workflow.user.UserProcessInstance;
import org.cmdbuild.workflow.user.UserWorkflowEngine;

public interface QueryableUserWorkflowEngine extends UserWorkflowEngine {

	PagedElements<UserProcessInstance> query(String className, QueryOptions queryOptions);

}
