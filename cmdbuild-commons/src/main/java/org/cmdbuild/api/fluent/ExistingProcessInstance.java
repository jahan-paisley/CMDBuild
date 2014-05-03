package org.cmdbuild.api.fluent;

import org.cmdbuild.api.fluent.FluentApiExecutor.AdvanceProcess;

public class ExistingProcessInstance extends ActiveCard {

	ExistingProcessInstance(final FluentApi api, final String className, final Integer processId) {
		super(api, className, processId);
	}

	public ExistingProcessInstance withProcessInstanceId(final String value) {
		super.set("ProcessCode", value);
		return this;
	}

	public ExistingProcessInstance withDescription(final String value) {
		super.setDescription(value);
		return this;
	}

	public ExistingProcessInstance with(final String name, final Object value) {
		return withAttribute(name, value);
	}

	public ExistingProcessInstance withAttribute(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public void update() {
		api().getExecutor().updateProcessInstance(this, AdvanceProcess.NO);
	}

	public void advance() {
		api().getExecutor().updateProcessInstance(this, AdvanceProcess.YES);
	}
}
