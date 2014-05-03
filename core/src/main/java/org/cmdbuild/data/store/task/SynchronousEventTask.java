package org.cmdbuild.data.store.task;

public class SynchronousEventTask extends Task {

	public static Builder<SynchronousEventTask> newInstance() {
		return new Builder<SynchronousEventTask>() {

			@Override
			protected SynchronousEventTask doBuild() {
				return new SynchronousEventTask(this);
			}

		};
	}

	private SynchronousEventTask(final Builder<? extends Task> builder) {
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
