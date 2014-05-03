package org.cmdbuild.data.store.task;

public class StartWorkflowTask extends Task {

	public static Builder<StartWorkflowTask> newInstance() {
		return new Builder<StartWorkflowTask>() {

			@Override
			protected StartWorkflowTask doBuild() {
				return new StartWorkflowTask(this);
			}

		};
	}

	private StartWorkflowTask(final Builder<? extends Task> builder) {
		super(builder);
	}

	@Override
	public void accept(final TaskVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected Builder<? extends Task> builder() {
		return newInstance();
	}

}
