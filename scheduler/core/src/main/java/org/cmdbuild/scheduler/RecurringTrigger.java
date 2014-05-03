package org.cmdbuild.scheduler;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class RecurringTrigger implements Trigger {

	public static RecurringTrigger at(final String cronExpression) {
		return new RecurringTrigger(cronExpression);
	}

	private final String cronExpression;

	private RecurringTrigger(final String cronExpression) {
		this.cronExpression = cronExpression;
	}

	@Override
	public void accept(final TriggerVisitor visitor) {
		visitor.visit(this);
	}

	public String getCronExpression() {
		return cronExpression;
	}

	@Override
	public int hashCode() {
		return cronExpression.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof RecurringTrigger)) {
			return false;
		}
		final RecurringTrigger other = RecurringTrigger.class.cast(obj);
		return cronExpression.equals(other.cronExpression);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
