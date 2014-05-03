package org.cmdbuild.model.data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.data.store.Storable;

public class Metadata implements Storable {

	public static Metadata of(final String name) {
		return of(name, null);
	}

	public static Metadata of(final String name, final String value) {
		return new Metadata(name, value);
	}

	public final String name;
	public final String value;

	private transient String toString;

	public Metadata(final String name, final String value) {
		this.name = name;
		this.value = value;

		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public String getIdentifier() {
		return name;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof Metadata)) {
			return false;
		}
		final Metadata other = Metadata.class.cast(obj);
		return name.equals(other.name) && value.equals(other.value);
	}

	@Override
	public String toString() {
		return toString;
	}

}
