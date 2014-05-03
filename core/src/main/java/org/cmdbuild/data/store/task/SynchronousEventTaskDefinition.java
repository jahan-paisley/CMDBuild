package org.cmdbuild.data.store.task;

public class SynchronousEventTaskDefinition extends TaskDefinition {

	public static Builder<SynchronousEventTaskDefinition> newInstance() {
		return new Builder<SynchronousEventTaskDefinition>() {

			@Override
			protected SynchronousEventTaskDefinition doBuild() {
				return new SynchronousEventTaskDefinition(this);
			}

		};
	}

	private SynchronousEventTaskDefinition(final Builder<? extends TaskDefinition> builder) {
		super(builder);
	}

	@Override
	public void accept(final TaskDefinitionVisitor visitor) {
		visitor.visit(this);
	}

}
