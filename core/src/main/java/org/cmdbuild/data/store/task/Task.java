package org.cmdbuild.data.store.task;

import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;

import com.google.common.collect.Maps;

public abstract class Task implements Storable {

	public static abstract class Builder<T extends Task> implements org.apache.commons.lang3.builder.Builder<T> {

		private static final Map<String, String> EMPTY = Collections.emptyMap();

		private Long id;
		private String description;
		private String cronExpression;
		private Boolean running;
		private Map<String, String> parameters = Maps.newHashMap();

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
			parameters = (parameters == null) ? EMPTY : parameters;
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

		public Builder<T> withRunningStatus(final Boolean running) {
			this.running = running;
			return this;
		}

		public Builder<T> withCronExpression(final String cronExpression) {
			this.cronExpression = cronExpression;
			return this;
		}

		public Builder<T> withParameters(final Map<String, String> parameters) {
			this.parameters.putAll(parameters);
			return this;
		}

		public Builder<T> withParameter(final String key, final String value) {
			this.parameters.put(key, value);
			return this;
		}

	}

	private final Long id;
	private final String description;
	private final boolean running;
	private final String cronExpression;
	private final Map<String, String> parameters;

	protected Task(final Builder<? extends Task> builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.running = builder.running;
		this.cronExpression = builder.cronExpression;
		this.parameters = builder.parameters;
	}

	public abstract void accept(final TaskVisitor visitor);

	public Builder<? extends Task> modify() {
		return builder() //
				.withId(id) //
				.withDescription(description) //
				.withRunningStatus(running) //
				.withCronExpression(cronExpression) //
				.withParameters(Maps.newHashMap(parameters));
	}

	protected abstract Builder<? extends Task> builder();

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

	// TODO move to parameters
	public String getCronExpression() {
		return cronExpression;
	}

	// TODO use something different from Map
	public Map<String, String> getParameters() {
		return parameters;
	}

	public String getParameter(final String key) {
		return parameters.get(key);
	}

}
