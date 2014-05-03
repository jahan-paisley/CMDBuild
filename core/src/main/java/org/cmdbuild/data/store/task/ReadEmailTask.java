package org.cmdbuild.data.store.task;

public class ReadEmailTask extends Task {

	public static Builder<ReadEmailTask> newInstance() {
		return new Builder<ReadEmailTask>() {

			@Override
			protected ReadEmailTask doBuild() {
				return new ReadEmailTask(this);
			}

		};
	}

	private ReadEmailTask(final Builder<? extends Task> builder) {
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
