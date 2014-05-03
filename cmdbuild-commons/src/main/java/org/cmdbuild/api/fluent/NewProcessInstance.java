package org.cmdbuild.api.fluent;

import org.cmdbuild.api.fluent.FluentApiExecutor.AdvanceProcess;

public class NewProcessInstance extends ActiveCard {

	NewProcessInstance(final FluentApi api, final String className) {
		super(api, className, null);
	}

	public NewProcessInstance withDescription(final String value) {
		super.setDescription(value);
		return this;
	}

	public NewProcessInstance with(final String name, final Object value) {
		return withAttribute(name, value);
	}

	public NewProcessInstance withAttribute(final String name, final Object value) {
		super.set(name, value);
		return this;
	}

	public ProcessInstanceDescriptor start() {
		return api().getExecutor().createProcessInstance(this, AdvanceProcess.NO);
	}

	public ProcessInstanceDescriptor startAndAdvance() {
		return api().getExecutor().createProcessInstance(this, AdvanceProcess.YES);
	}
}
