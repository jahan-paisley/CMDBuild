package org.cmdbuild.dao.entrytype.attributetype;

import org.apache.commons.lang3.StringUtils;
import org.cmdbuild.dao.entry.IdAndDescription;

public abstract class AbstractReferenceAttributeType extends AbstractAttributeType<IdAndDescription> {

	@Override
	protected IdAndDescription convertNotNullValue(final Object value) {
		if (value instanceof IdAndDescription) {
			return IdAndDescription.class.cast(value);
		}
		if (value instanceof Number) {
			return new IdAndDescription(Number.class.cast(value).longValue(), StringUtils.EMPTY);
		} else if (value instanceof String) {
			final Long converted;
			if (StringUtils.isBlank(String.class.cast(value))) {
				converted = null;
			} else {
				converted = Long.parseLong(String.class.cast(value));
			}
			return new IdAndDescription(converted, StringUtils.EMPTY);
		} else {
			throw illegalValue(value);
		}
	}

}
