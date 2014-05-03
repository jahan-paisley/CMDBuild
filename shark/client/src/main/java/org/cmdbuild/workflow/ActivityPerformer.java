package org.cmdbuild.workflow;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class ActivityPerformer {

	public static enum Type {
		ROLE, EXPRESSION, ADMIN, UNKNOWN // fake performer for our ugly stuff
	}

	private final String value;
	private final Type type;
	private final int hashCode;

	private ActivityPerformer(final Type type, final String value) {
		this.value = value;
		this.type = type;
		this.hashCode = new HashCodeBuilder().append(type).append(value).hashCode();
	}

	public Type getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public boolean isRole(final String name) {
		return (type == Type.ROLE) && (value.equals(name));
	}

	public boolean isExpression() {
		return (type == Type.EXPRESSION);
	}

	public boolean isAdmin() {
		return (type == Type.ADMIN);
	}

	public static ActivityPerformer newRolePerformer(final String name) {
		Validate.notNull(name);
		return new ActivityPerformer(Type.ROLE, name);
	}

	public static ActivityPerformer newExpressionPerformer(final String expression) {
		Validate.notNull(expression);
		return new ActivityPerformer(Type.EXPRESSION, expression);
	}

	public static ActivityPerformer newAdminPerformer() {
		return new ActivityPerformer(Type.ADMIN, null);
	}

	public static ActivityPerformer newUnknownPerformer() {
		return new ActivityPerformer(Type.UNKNOWN, null);
	}

	@Override
	public boolean equals(final Object object) {
		if (object == this) {
			return true;
		}
		if (!(object instanceof ActivityPerformer)) {
			return false;
		}
		final ActivityPerformer activityPerformer = ActivityPerformer.class.cast(object);
		return (type.equals(activityPerformer.type) && (value.equals(activityPerformer.value)));
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this) //
				.append("type", type) //
				.append("value", value) //
				.toString();
	}

}
