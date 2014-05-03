package org.cmdbuild.dao.entrytype.attributetype;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.cmdbuild.dao.entry.IdAndDescription;
import org.cmdbuild.dao.entry.LookupValue;

public class LookupAttributeType extends AbstractReferenceAttributeType {

	private final String lookupTypeName;
	private final transient String toString;

	public LookupAttributeType(final String lookupTypeName) {
		this.lookupTypeName = lookupTypeName;
		this.toString = ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public String getLookupTypeName() {
		return lookupTypeName;
	}

	@Override
	public void accept(final CMAttributeTypeVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected LookupValue convertNotNullValue(final Object value) {
		if (value instanceof IdAndDescription) {
			return new LookupValue( //
					((IdAndDescription) value).getId(), //
					((IdAndDescription) value).getDescription(), //
					lookupTypeName);
		}

		if (value instanceof Number) {
			return new LookupValue( //
					Number.class.cast(value).longValue(), StringUtils.EMPTY, lookupTypeName);
		} else if (value instanceof String) {
			final String s = String.class.cast(value);
			if (isNotBlank(s)) {
				return new LookupValue( //
						Long.parseLong(s), StringUtils.EMPTY, lookupTypeName);
			} else {
				return null;
			}
		} else {
			throw illegalValue(value);
		}
	}

	@Override
	public String toString() {
		return toString;
	}

}
