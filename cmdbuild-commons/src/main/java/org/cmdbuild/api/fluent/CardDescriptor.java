package org.cmdbuild.api.fluent;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CardDescriptor {

	private final String className;
	private final Integer id;

	private final transient Integer hashCode;
	private final transient String toString;

	public CardDescriptor(final String className, final Integer id) {
		this.className = className;
		this.id = id;

		this.hashCode = new HashCodeBuilder() //
				.append(className) //
				.append(id) //
				.hashCode();
		this.toString = new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE) //
				.append("classname", className) //
				.append("id", id) //
				.toString();
	}

	public String getClassName() {
		return className;
	}

	public Integer getId() {
		return id;
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof CardDescriptor)) {
			return false;
		}
		final CardDescriptor descriptor = CardDescriptor.class.cast(object);
		return (className.equals(descriptor.className) && (id == descriptor.id));
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return toString;
	}

}
