package org.cmdbuild.model.data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ClassOrder {

	public final String attributeName;
	public final int value;
	private final transient String toString;

	private ClassOrder(final String attributeName, final int value) {
		this.attributeName = attributeName;
		this.value = value;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public boolean equals(final Object obj) {
		return attributeName.equals(obj);
	}

	@Override
	public int hashCode() {
		return attributeName.hashCode();
	}

	@Override
	public String toString() {
		return toString;
	}

	public static ClassOrder from(final String attributeName, final int value) {
		return new ClassOrder(attributeName, value);
	}

}
