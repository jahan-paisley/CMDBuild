package org.cmdbuild.data.store.task;

public class StartWorkflowTaskDefinition extends TaskDefinition {

	public static Builder<StartWorkflowTaskDefinition> newInstance() {
		return new Builder<StartWorkflowTaskDefinition>() {

			@Override
			protected StartWorkflowTaskDefinition doBuild() {
				return new StartWorkflowTaskDefinition(this);
			}

		};
	}

	private StartWorkflowTaskDefinition(final Builder<? extends TaskDefinition> builder) {
		super(builder);
	}

	@Override
	public void accept(final TaskDefinitionVisitor visitor) {
		visitor.visit(this);
	}

}
