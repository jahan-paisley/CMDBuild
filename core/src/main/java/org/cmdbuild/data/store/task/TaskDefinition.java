package org.cmdbuild.data.store.task;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;

public abstract class TaskDefinition implements Storable {

	public static abstract class Builder<T extends TaskDefinition> implements
			org.apache.commons.lang3.builder.Builder<T> {

		private Long id;
		private String description;
		private String cronExpression;
		private Boolean running;

		protected Builder() {
			// usable by subclasses only
		}

		@Override
		public T build() {
			validate();
			return doBuild();
		}

		private void validate() {
			running = (running == null) ? Boolean.FALSE : running;
		}

		protected abstract T doBuild();

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}

		public Builder<T> withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder<T> withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder<T> withRunning(final Boolean running) {
			this.running = running;
			return this;
		}

		public Builder<T> withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

	}

	private final Long id;
	private final String description;
	private final boolean running;
	private final String cronExpression;

	protected TaskDefinition(final Builder<? extends TaskDefinition> builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.running = builder.running;
		this.cronExpression = builder.cronExpression;
	}

	public abstract void accept(final TaskDefinitionVisitor visitor);

	@Override
	public String getIdentifier() {
		return id.toString();
	}

	public Long getId() {
		return id;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRunning() {
		return running;
	}

	public String getCronExpression() {
		return cronExpression;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
