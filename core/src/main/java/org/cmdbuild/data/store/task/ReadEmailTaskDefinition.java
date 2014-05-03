package org.cmdbuild.data.store.task;

public class ReadEmailTaskDefinition extends TaskDefinition {

	public static Builder<ReadEmailTaskDefinition> newInstance() {
		return new Builder<ReadEmailTaskDefinition>() {

			@Override
			protected ReadEmailTaskDefinition doBuild() {
				return new ReadEmailTaskDefinition(this);
			}

		};
	}

	private ReadEmailTaskDefinition(final Builder<? extends TaskDefinition> builder) {
		super(builder);
	}

	@Override
	public void accept(final TaskDefinitionVisitor visitor) {
		visitor.visit(this);
	}

}
