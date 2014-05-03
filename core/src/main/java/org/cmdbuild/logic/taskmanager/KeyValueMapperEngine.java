package org.cmdbuild.logic.taskmanager;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.Pair;

public class KeyValueMapperEngine implements MapperEngine {

	public static class Builder implements org.apache.commons.lang3.builder.Builder<KeyValueMapperEngine> {

		private Pair<String, String> key;
		private Pair<String, String> value;

		private Builder() {
			// use factory method
		}

		@Override
		public KeyValueMapperEngine build() {
			validate();
			return new KeyValueMapperEngine(this);
		}

		private void validate() {
			Validate.notNull(key, "missing key");
			Validate.notBlank(key.getLeft(), "invalid key init");
			Validate.notBlank(key.getRight(), "invalid key end");
			Validate.notNull(value, "missing value");
			Validate.notBlank(value.getLeft(), "invalid value init");
			Validate.notBlank(value.getRight(), "invalid value end");
		}

		public Builder withKey(final String init, final String end) {
			this.key = Pair.of(init, end);
			return this;
		}

		public Builder withValue(final String init, final String end) {
			this.value = Pair.of(init, end);
			return this;
		}

	}

	public static Builder newInstance() {
		return new Builder();
	}

	private final Pair<String, String> key;
	private final Pair<String, String> value;

	private KeyValueMapperEngine(final Builder builder) {
		this.key = builder.key;
		this.value = builder.value;
	}

	@Override
	public void accept(final MapperEngineVisitor visitor) {
		visitor.visit(this);
	}

	public String getKeyInit() {
		return key.getLeft();
	}

	public String getKeyEnd() {
		return key.getRight();
	}

	public String getValueInit() {
		return value.getLeft();
	}

	public String getValueEnd() {
		return value.getRight();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
