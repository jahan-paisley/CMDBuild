package org.cmdbuild.data.store.lookup;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.common.Builder;

public final class LookupType {

	public static class LookupTypeBuilder implements Builder<LookupType> {

		private String name;
		private String parent;

		/**
		 * instantiate using {@link LookupType#newInstance()}
		 */
		private LookupTypeBuilder() {
		}

		public LookupType.LookupTypeBuilder withName(final String value) {
			this.name = value;
			return this;
		}

		public LookupType.LookupTypeBuilder withParent(final String value) {
			this.parent = value;
			return this;
		}

		@Override
		public LookupType build() {
			this.name = defaultIfBlank(name, null);
			this.parent = defaultIfBlank(parent, null);

			return new LookupType(this);
		}

	}

	public static LookupType.LookupTypeBuilder newInstance() {
		return new LookupTypeBuilder();
	}

	public final String name;
	public final String parent;

	private final transient int hashCode;
	private final transient String toString;

	public LookupType(final LookupTypeBuilder builder) {
		this.name = builder.name;
		this.parent = builder.parent;

		this.hashCode = new HashCodeBuilder() //
				.append(this.name) //
				.append(this.parent) //
				.toHashCode();
		this.toString = new ToStringBuilder(this, ToStringStyle.SIMPLE_STYLE) //
				.append("name", name) //
				.append("parent", parent) //
				.toString();
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof LookupType)) {
			return false;
		}
		final LookupType other = LookupType.class.cast(obj);
		return new EqualsBuilder() //
				.append(name, other.name) //
				.append(parent, other.parent) //
				.isEquals();
	}

	@Override
	public String toString() {
		return toString;
	}

}
