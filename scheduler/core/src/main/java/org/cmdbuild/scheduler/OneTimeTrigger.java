package org.cmdbuild.scheduler;

import java.util.Date;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class OneTimeTrigger implements Trigger {

	public static OneTimeTrigger at(final Date date) {
		return new OneTimeTrigger(date);
	}

	private final Date date;

	private OneTimeTrigger(final Date date) {
		this.date = date;
	}

	@Override
	public void accept(final TriggerVisitor visitor) {
		visitor.visit(this);
	}

	public Date getDate() {
		return date;
	}

	@Override
	public int hashCode() {
		return date.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof OneTimeTrigger)) {
			return false;
		}
		final OneTimeTrigger other = OneTimeTrigger.class.cast(obj);
		return date.equals(other.date);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
