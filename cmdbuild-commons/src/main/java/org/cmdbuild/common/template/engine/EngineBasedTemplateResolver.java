package org.cmdbuild.common.template.engine;

import static java.util.Arrays.asList;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cmdbuild.common.template.TemplateResolver;

import com.google.common.collect.Maps;

/**
 * {@link TemplateResolver} based on {@link Engine}s.
 */
public class EngineBasedTemplateResolver implements TemplateResolver {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<EngineBasedTemplateResolver> {

		private final Map<String, Engine> engines = Maps.newHashMap();

		private Builder() {
			// use factory method
		}

		public Builder withEngine(final Engine engine, final String... prefixes) {
			return withEngine(engine, asList(prefixes));
		}

		public Builder withEngine(final Engine engine, final Iterable<String> prefixes) {
			for (final String p : prefixes) {
				engines.put(p, engine);
			}
			return this;
		}

		@Override
		public EngineBasedTemplateResolver build() {
			return new EngineBasedTemplateResolver(this);
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final Pattern VAR_PATTERN = Pattern.compile("([^\\{]+)?(\\{(\\w+):(\\w+)\\})?");

	private final Map<String, Engine> engines;

	private EngineBasedTemplateResolver(final Builder builder) {
		this.engines = builder.engines;
	}

	@Override
	public String resolve(final String template) {
		final StringBuilder sb = new StringBuilder();
		final Matcher matcher = VAR_PATTERN.matcher(template);
		while (matcher.find()) {
			final String nonvarPart = matcher.group(1);
			final String varPart = matcher.group(2);
			if (nonvarPart != null) {
				sb.append(nonvarPart);
			}
			if (varPart != null) {
				final String enginePrefix = matcher.group(3);
				final String variable = matcher.group(4);
				final Object value = expandVariable(enginePrefix, variable);
				sb.append(String.valueOf(value));
			}
		}
		return sb.toString();
	}

	private Object expandVariable(final String enginePrefix, final String variable) {
		final Engine e = engines.get(enginePrefix);
		if (e != null) {
			return e.eval(variable);
		} else {
			return null;
		}
	}

}
