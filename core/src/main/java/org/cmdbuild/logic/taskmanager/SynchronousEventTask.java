package org.cmdbuild.logic.taskmanager;

import java.util.Collections;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class SynchronousEventTask implements Task {

	public static enum Phase {
		AFTER_CREATE {
			@Override
			public void identify(final PhaseIdentifier identifier) {
				identifier.afterCreate();
			}
		}, //
		BEFORE_UPDATE {
			@Override
			public void identify(final PhaseIdentifier identifier) {
				identifier.beforeUpdate();
			}
		}, //
		AFTER_UPDATE {
			@Override
			public void identify(final PhaseIdentifier identifier) {
				identifier.afterUpdate();
			}
		}, //
		BEFORE_DELETE {
			@Override
			public void identify(final PhaseIdentifier identifier) {
				identifier.beforeDelete();
			}
		}, //
		;

		/**
		 * Simulates in some way the use of the visitor pattern.
		 */
		public abstract void identify(PhaseIdentifier identifier);

	}

	public static interface PhaseIdentifier {

		void afterCreate();

		void beforeDelete();

		void afterUpdate();

		void beforeUpdate();

	}

	public static class Builder implements org.apache.commons.lang3.builder.Builder<SynchronousEventTask> {

		private static final Iterable<String> EMPTY_GROUPS = Collections.emptyList();

		private Long id;
		private String description;
		private Boolean active;
		private Phase phase;
		private Iterable<String> groups;
		private String classname;
		private Boolean scriptingEnabled;
		private String scriptingEngine;
		private String scriptingScript;
		private Boolean scriptingSafe;

		private Builder() {
			// use factory method
		}

		@Override
		public SynchronousEventTask build() {
			validate();
			return new SynchronousEventTask(this);
		}

		private void validate() {
			active = (active == null) ? false : active;

			groups = (groups == null) ? EMPTY_GROUPS : groups;

			scriptingEnabled = (scriptingEnabled == null) ? false : scriptingEnabled;
			if (scriptingEnabled) {
				Validate.notBlank(scriptingEngine, "missing scripting engine");
			}
			scriptingSafe = (scriptingSafe == null) ? false : scriptingSafe;
		}

		public Builder withId(final Long id) {
			this.id = id;
			return this;
		}

		public Builder withDescription(final String description) {
			this.description = description;
			return this;
		}

		public Builder withActiveStatus(final boolean active) {
			this.active = active;
			return this;
		}

		public Builder withPhase(final Phase phase) {
			this.phase = phase;
			return this;
		}

		public Builder withGroups(final Iterable<String> groups) {
			this.groups = groups;
			return this;
		}

		public Builder withTargetClass(final String classname) {
			this.classname = classname;
			return this;
		}

		public Builder withScriptingEnableStatus(final boolean scriptingEnabled) {
			this.scriptingEnabled = scriptingEnabled;
			return this;
		}

		public Builder withScriptingEngine(final String scriptingEngine) {
			this.scriptingEngine = scriptingEngine;
			return this;
		}

		public Builder withScript(final String scriptingScript) {
			this.scriptingScript = scriptingScript;
			return this;
		}

		public Builder withScriptingSafeStatus(final boolean scriptingSafe) {
			this.scriptingSafe = scriptingSafe;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Long id;
	private final String description;
	private final boolean active;
	private final Phase phase;
	private final Iterable<? extends String> groups;
	private final String classname;
	private final boolean scriptingEnabled;
	private final String scriptingEngine;
	private final String scriptingScript;
	private final boolean scriptingSafe;

	private SynchronousEventTask(final Builder builder) {
		this.id = builder.id;
		this.description = builder.description;
		this.active = builder.active;
		this.phase = builder.phase;
		this.groups = builder.groups;
		this.classname = builder.classname;
		this.scriptingEnabled = builder.scriptingEnabled;
		this.scriptingEngine = builder.scriptingEngine;
		this.scriptingScript = builder.scriptingScript;
		this.scriptingSafe = builder.scriptingSafe;
	}

	@Override
	public void accept(final TaskVistor visitor) {
		visitor.visit(this);
	}

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public boolean isActive() {
		return active;
	}

	public Phase getPhase() {
		return phase;
	}

	public Iterable<? extends String> getGroups() {
		return groups;
	}

	public String getTargetClassname() {
		return classname;
	}

	public boolean isScriptingEnabled() {
		return scriptingEnabled;
	}

	public String getScriptingEngine() {
		return scriptingEngine;
	}

	public String getScriptingScript() {
		return scriptingScript;
	}

	public boolean isScriptingSafe() {
		return scriptingSafe;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
