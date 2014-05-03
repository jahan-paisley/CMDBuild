package org.cmdbuild.workflow.api;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.cmdbuild.common.Constants.DATETIME_PRINTING_PATTERN;
import static org.cmdbuild.common.Constants.DATE_PRINTING_PATTERN;
import static org.cmdbuild.common.Constants.SOAP_ALL_DATES_PRINTING_PATTERN;
import static org.cmdbuild.common.Constants.TIME_PRINTING_PATTERN;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.cmdbuild.api.fluent.ws.WsFluentApiExecutor.WsType;
import org.cmdbuild.shark.Logging;
import org.cmdbuild.workflow.SharkTypeDefaults;
import org.cmdbuild.workflow.type.LookupType;
import org.cmdbuild.workflow.type.ReferenceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SharkWsTypeConverter {

	protected static final Logger logger = LoggerFactory.getLogger(Logging.LOGGER_NAME);

	protected final WorkflowApi workflowApi;

	public SharkWsTypeConverter(final WorkflowApi workflowApi) {
		this.workflowApi = workflowApi;
	}

	protected String toWsType(final WsType wsType, final Object value) {
		if (value == null) {
			return EMPTY;
		}
		try {
			return unsafeToWsType(wsType, value);
		} catch (final RuntimeException e) {
			logger.warn("error converting to ws type, continuing anyway passing stringified value", e);
			return value.toString();
		}
	}

	private String unsafeToWsType(final WsType wsType, final Object value) {
		switch (wsType) {
		case DATE:
		case TIMESTAMP:
		case TIME:
			return new SimpleDateFormat(SOAP_ALL_DATES_PRINTING_PATTERN).format((Date) value);

		case FOREIGNKEY:
		case REFERENCE:
			final ReferenceType reference = (ReferenceType) value;
			if (reference.checkValidity()) {
				return Integer.toString(reference.getId());
			} else {
				return EMPTY;
			}

		case LOOKUP:
			final LookupType lookup = (LookupType) value;
			if (lookup.checkValidity()) {
				return Integer.toString(lookup.getId());
			} else {
				return EMPTY;
			}

		default:
			return value.toString();
		}
	}

	protected Object toClientType(final WsType wsType, final String wsValue) {
		try {
			return unsafeToClientType(wsType, wsValue);
		} catch (final Exception e) {
			throw new IllegalArgumentException("unexpected value format", e);
		}
	}

	protected Object unsafeToClientType(final WsType wsType, final String wsValue) throws NumberFormatException,
			ParseException {
		switch (wsType) {
		case BOOLEAN:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultBoolean() : Boolean.parseBoolean(wsValue);

		case DATE:
		case TIMESTAMP:
		case TIME:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultDate() : parseAllDateFormats(wsValue);

		case DECIMAL:
		case DOUBLE:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultDouble() : Double.parseDouble(wsValue);

		case FOREIGNKEY:
		case REFERENCE:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultReference() : referenceType(wsValue);

		case INTEGER:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultInteger() : Long.parseLong(wsValue);

		case LOOKUP:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultLookup() : lookupType(wsValue);

		case CHAR:
		case STRING:
		case TEXT:
			return isBlank(wsValue) ? SharkTypeDefaults.defaultString() : wsValue;

		default:
			return wsValue;
		}
	}

	/**
	 * We should be able to parse timestamp/date/time values in all available
	 * formats since we don't have a unique one.
	 */
	private Date parseAllDateFormats(final String wsValue) throws ParseException {
		final String[] formats = new String[] { SOAP_ALL_DATES_PRINTING_PATTERN, DATETIME_PRINTING_PATTERN,
				DATE_PRINTING_PATTERN, TIME_PRINTING_PATTERN };
		for (final String format : formats) {
			try {
				logger.debug("trying parsing using format '{}'", format);
				return new SimpleDateFormat(format).parse(wsValue);
			} catch (final ParseException e) {
				logger.warn(format("error parsing using format '%s', trying next one", format), e);
			}
		}
		logger.warn("error parsing using all formats");
		return null;
	}

	private ReferenceType referenceType(final String wsValue) {
		final Integer id = Integer.parseInt(wsValue);
		return workflowApi.referenceTypeFrom(id);
	}

	private LookupType lookupType(final String wsValue) {
		final Integer id = Integer.parseInt(wsValue);
		return workflowApi.selectLookupById(id);
	}

}