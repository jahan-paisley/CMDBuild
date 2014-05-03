package org.cmdbuild.logic.taskmanager;

import static java.lang.String.format;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;

import com.google.common.collect.Maps;

public class EngineBasedMapper implements Mapper, MapperEngineVisitor {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<EngineBasedMapper> {

		private String text;
		private MapperEngine engine;

		private Builder() {
			// use factory method
		}

		@Override
		public EngineBasedMapper build() {
			validate();
			return new EngineBasedMapper(this);
		}

		private void validate() {
			Validate.notNull(text, "missing text");
			Validate.notNull(engine, "missing engine");
		}

		public Builder withText(final String text) {
			this.text = text;
			return this;
		}

		public Builder withEngine(final MapperEngine engine) {
			this.engine = engine;
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private static final String KEY_VALUE_PATTERN_TEMPLATE = "^.*<%s>(.*)<\\/%s>.*<%s>(.*)<\\/%s>.*$";

	private final String text;
	private final MapperEngine engine;
	private final Map<String, String> map;

	private EngineBasedMapper(final Builder builder) {
		this.text = builder.text;
		this.engine = builder.engine;
		this.map = Maps.newHashMap();
	}

	@Override
	public Map<String, String> map() {
		engine.accept(this);
		return map;
	}

	@Override
	public void visit(final KeyValueMapperEngine mapper) {
		final Pattern pattern = Pattern.compile( //
				format(KEY_VALUE_PATTERN_TEMPLATE, //
						Pattern.quote(mapper.getKeyInit()), //
						Pattern.quote(mapper.getKeyEnd()), //
						Pattern.quote(mapper.getValueInit()), //
						Pattern.quote(mapper.getValueEnd()) //
				), //
				Pattern.MULTILINE);
		final Matcher matcher = pattern.matcher(text);
		while (matcher.find()) {
			final String key = matcher.group(1);
			final String value = matcher.group(2);
			map.put(key, value);
		}
	}

	@Override
	public void visit(final NullMapperEngine mapper) {
		// nothing to do
	}

}
