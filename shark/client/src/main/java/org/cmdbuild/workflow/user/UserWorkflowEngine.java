package org.cmdbuild.workflow.user;

import org.cmdbuild.workflow.CMActivityInstance;
import org.cmdbuild.workflow.CMProcessClass;
import org.cmdbuild.workflow.CMWorkflowEngine;
import org.cmdbuild.workflow.CMWorkflowException;

public interface UserWorkflowEngine extends CMWorkflowEngine {

	@Override
	UserProcessClass findProcessClassById(Long id);

	@Override
	UserProcessClass findProcessClassByName(String name);

	@Override
	Iterable<UserProcessClass> findProcessClasses();

	@Override
	Iterable<UserProcessClass> findAllProcessClasses();

	@Override
	UserProcessInstance startProcess(CMProcessClass type) throws CMWorkflowException;

	@Override
	UserProcessInstance advanceActivity(CMActivityInstance activityInstance) throws CMWorkflowException;

	UserProcessInstance findProcessInstance(CMProcessClass processClass, Long cardId);

}
