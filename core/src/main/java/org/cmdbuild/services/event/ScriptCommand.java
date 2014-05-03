package org.cmdbuild.services.event;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.io.Reader;
import java.io.StringReader;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.apache.commons.lang3.Validate;
import org.cmdbuild.api.fluent.FluentApi;
import org.cmdbuild.services.event.Contexts.AfterCreate;
import org.cmdbuild.services.event.Contexts.AfterUpdate;
import org.cmdbuild.services.event.Contexts.BeforeDelete;
import org.cmdbuild.services.event.Contexts.BeforeUpdate;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class ScriptCommand implements Command {

	private static final Marker marker = MarkerFactory.getMarker(ScriptCommand.class.getName());

	private static final String CURRENT = "__current__";
	private static final String LOGGER = "__logger__";
	private static final String NEXT = "__next__";
	private static final String PREVIOUS = "__previous__";

	private static final String CMDB = "__cmdb__";
	private static final String CMDB_V1 = "__cmdb_v1__";

	public static class Builder implements org.cmdbuild.common.Builder<ScriptCommand> {

		private String engine;
		private String script;
		private FluentApi fluentApi;

		private Builder() {
			// use factory method
		}

		@Override
		public ScriptCommand build() {
			validate();
			return new ScriptCommand(this);
		}

		private void validate() {
			Validate.notBlank(engine, "invalid engine");
			script = defaultString(script);
			Validate.notNull(fluentApi, "invalid api");
		}

		public Builder withEngine(final String engine) {
			this.engine = engine;
			return this;
		}

		public Builder withScript(final String script) {
			this.script = script;
			return this;
		}

		public Builder withFluentApi(final FluentApi fluentApi) {
			this.fluentApi = fluentApi;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final String engine;
	private final String script;
	private final FluentApi fluentApi;

	private ScriptCommand(final Builder builder) {
		this.engine = builder.engine;
		this.script = builder.script;
		this.fluentApi = builder.fluentApi;
	}

	@Override
	public void execute(final Context context) {
		try {
			final ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
			final Bindings bindings = scriptEngineManager.getBindings();
			fillBindings(bindings, context);
			final ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(engine);
			final Reader reader = new StringReader(script);
			scriptEngine.eval(reader, bindings);
		} catch (final Exception e) {
			logger.warn(marker, "cannot execute script", e);
			throw new RuntimeException(e);
		}
	}

	private void fillBindings(final Bindings bindings, final Context context) {
		bindings.put(CMDB, fluentApi);
		bindings.put(CMDB_V1, fluentApi);
		bindings.put(LOGGER, Command.logger);

		context.accept(new ContextVisitor() {
			@Override
			public void visit(final AfterCreate context) {
				bindings.put(CURRENT, context.card);
			}

			@Override
			public void visit(final BeforeUpdate context) {
				bindings.put(CURRENT, context.actual);
				bindings.put(NEXT, context.next);
			}

			@Override
			public void visit(final AfterUpdate context) {
				bindings.put(PREVIOUS, context.previous);
				bindings.put(CURRENT, context.actual);
			}

			@Override
			public void visit(final BeforeDelete context) {
				bindings.put(CURRENT, context.card);
			}

		});
	}

}
