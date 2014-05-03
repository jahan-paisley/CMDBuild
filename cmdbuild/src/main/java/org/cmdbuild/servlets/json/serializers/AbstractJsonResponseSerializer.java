package org.cmdbuild.servlets.json.serializers;

import org.cmdbuild.dao.entrytype.attributetype.CMAttributeType;
import org.joda.time.DateTime;
import org.json.JSONException;

public abstract class AbstractJsonResponseSerializer {

	protected final String formatDateTime(final DateTime dateTime) {
		if (dateTime == null) {
			return null;
		} else {
			return JavaToJSONValueConverter.DATE_TIME_FORMATTER.print(dateTime);
		}
	}

	protected Object javaToJsonValue( //
			final CMAttributeType<?> type, //
			final Object value //
		) throws JSONException {

		return new JavaToJSONValueConverter(type, value).valueForJson();
	}

}
