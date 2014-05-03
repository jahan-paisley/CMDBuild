package org.cmdbuild.workflow;

import java.util.List;

import org.cmdbuild.dao.entry.ForwardingCard;
import org.cmdbuild.workflow.service.WSProcessDefInfo;
import org.cmdbuild.workflow.service.WSProcessInstanceState;

public abstract class ForwardingProcessInstance extends ForwardingCard implements CMProcessInstance {

	private final CMProcessInstance delegate;

	protected ForwardingProcessInstance(final CMProcessInstance delegate) {
		super(delegate);
		this.delegate = delegate;
	}

	@Override
	public CMProcessClass getType() {
		return delegate.getType();
	}

	@Override
	public Long getCardId() {
		return delegate.getCardId();
	}

	@Override
	public String getProcessInstanceId() {
		return delegate.getProcessInstanceId();
	}

	@Override
	public WSProcessInstanceState getState() {
		return delegate.getState();
	}

	@Override
	public List<? extends CMActivityInstance> getActivities() {
		return delegate.getActivities();
	}

	@Override
	public CMActivityInstance getActivityInstance(final String activityInstanceId) {
		return delegate.getActivityInstance(activityInstanceId);
	}

	@Override
	public WSProcessDefInfo getUniqueProcessDefinition() {
		return delegate.getUniqueProcessDefinition();
	}

}
