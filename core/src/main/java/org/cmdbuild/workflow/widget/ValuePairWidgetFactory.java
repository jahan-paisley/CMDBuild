package org.cmdbuild.workflow.widget;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.cmdbuild.cql.compiler.impl.QueryImpl;
import org.cmdbuild.cql.facade.CQLFacade;
import org.cmdbuild.dao.entry.CMValueSet;
import org.cmdbuild.exception.CMDBWorkflowException;
import org.cmdbuild.exception.CMDBWorkflowException.WorkflowExceptionType;
import org.cmdbuild.model.widget.Widget;
import org.cmdbuild.notification.Notifier;
import org.cmdbuild.services.template.engine.EngineNames;
import org.cmdbuild.services.template.store.TemplateRepository;
import org.cmdbuild.workflow.CMActivityWidget;
import org.cmdbuild.workflow.xpdl.SingleActivityWidgetFactory;

/**
 * Single activity widget factory that knows how to decode a list of key/value
 * pairs.
 */
public abstract class ValuePairWidgetFactory implements SingleActivityWidgetFactory {

	public static final String BUTTON_LABEL = "ButtonLabel";

	/**
	 * Key in the value map that holds the output variable name.
	 */
	public static final String OUTPUT_KEY = null;

	private static final String LINE_SEPARATOR = "\r?\n";
	private static final String VALUE_SEPARATOR = "=";

	private static final String FILTER_KEY = "Filter";
	private static final String SINGLE_QUOTES = "'";
	private static final String DOUBLE_QUOTES = "\"";
	private static final String CLIENT_PREFIX = EngineNames.CLIENT + ":";
	private static final String DB_TEMPLATE_PREFIX = EngineNames.DB_TEMPLATE + ":";

	private final TemplateRepository templateRespository;
	private final Notifier notifier;

	protected ValuePairWidgetFactory(final TemplateRepository templateRespository, final Notifier notifier) {
		Validate.notNull(templateRespository);
		this.templateRespository = templateRespository;
		this.notifier = notifier;
	}

	@Override
	public final CMActivityWidget createWidget(final String serialization, final CMValueSet processInstanceVariables) {
		Widget widget;
		try {
			final Map<String, Object> valueMap = deserialize(serialization, processInstanceVariables);
			widget = createWidget(valueMap);
			setWidgetId(widget, serialization);
			setWidgetLabel(widget, valueMap);
		} catch (final Exception e) {
			widget = null;
			notifier.warn(new CMDBWorkflowException(WorkflowExceptionType.WF_CANNOT_CONFIGURE_CMDBEXTATTR,
					getWidgetName()));
		}

		return widget;
	}

	private void setWidgetId(final Widget widget, final String serialization) {
		final String id = String.format("widget-%x", serialization.hashCode());
		widget.setStringId(id);
	}

	private void setWidgetLabel(final Widget widget, final Map<String, Object> valueMap) {
		final String label = (String) valueMap.get(BUTTON_LABEL);
		if (label != null) {
			widget.setLabel(label);
		}
	}

	private Map<String, Object> deserialize(final String serialization, final CMValueSet processInstanceVariables) {
		final Map<String, Object> valueMap = new HashMap<String, Object>();
		for (final String line : serialization.split(LINE_SEPARATOR)) {
			addPair(valueMap, line, processInstanceVariables);
		}
		return valueMap;
	}

	private void addPair(final Map<String, Object> valueMap, final String line,
			final CMValueSet processInstanceVariables) {
		final String pair[] = line.split(VALUE_SEPARATOR, 2);
		if (pair.length > 0) {
			final String key = pair[0].trim();
			if (key.isEmpty()) {
				return;
			}
			final String valueString = (pair.length == 1) ? StringUtils.EMPTY : pair[1].trim();
			if (valueString.isEmpty()) {
				valueMap.put(OUTPUT_KEY, key);
			} else {
				final Object value = interpretValue(key, valueString, processInstanceVariables);
				valueMap.put(key, value);
			}
		}
	}

	private Object interpretValue(final String key, final String value, final CMValueSet processInstanceVariables) {
		if (betweenQuotes(value)) {
			// Quoted values are interpreted as strings
			return value.substring(1, value.length() - 1);
		} else if (FILTER_KEY.equals(key)) {
			// Filter (!) interpreted as a string even if not quoted
			return value;
		} else if (Character.isDigit(value.charAt(0))) {
			return readInteger(value);
		} else if (value.startsWith(CLIENT_PREFIX)) {
			// "Client" variables are always interpreted by the
			// template resolver on the client side
			return String.format("{%s}", value);
		} else if (value.startsWith(DB_TEMPLATE_PREFIX)) {
			final String templateName = value.substring(DB_TEMPLATE_PREFIX.length());
			return templateRespository.getTemplate(templateName);
		} else {
			// Process variables are fetched from the process instance
			return processInstanceVariables.get(value);
		}
	}

	private boolean betweenQuotes(final String value) {
		return (value.startsWith(DOUBLE_QUOTES) && value.endsWith(DOUBLE_QUOTES))
				|| (value.startsWith(SINGLE_QUOTES) && value.endsWith(SINGLE_QUOTES));
	}

	protected abstract Widget createWidget(Map<String, Object> valueMap);

	protected final String readString(final Object value) {
		if (value instanceof String) {
			return (String) value;
		} else if (value != null) {
			return value.toString();
		} else {
			return null;
		}
	}

	protected final String[] readCommaSeparatedString(final Object value) {
		final String stringValue = readString(value);
		String[] out = null;
		if (stringValue != null) {
			out = stringValue.split(",");
		}

		return out;
	}

	protected final boolean readBooleanTrueIfPresent(final Object value) {
		return (value != null);
	}

	protected final boolean readBooleanTrueIfTrue(final Object value) {
		if (value instanceof String) {
			return Boolean.parseBoolean((String) value);
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else {
			return false;
		}
	}

	protected final Integer readInteger(final Object value) {
		if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (final NumberFormatException e) {
				return null;
			}
		} else if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof Number) {
			return ((Number) value).intValue();
		} else {
			return null;
		}
	}

	protected final String readClassNameFromCQLFilter(final Object filter) {
		if (filter instanceof String) {
			try {
				final QueryImpl q = CQLFacade.compileWithTemplateParams((String) filter);
				return q.getFrom().mainClass().getClassName();
			} catch (final Exception e) {
				// return null later
			}
		}
		return null;
	}

	protected final Map<String, Object> extractUnmanagedParameters(final Map<String, Object> valueMap,
			final Set<String> managedParameters) {
		final Map<String, Object> out = new HashMap<String, Object>();

		for (final String key : valueMap.keySet()) {
			if (key == null || managedParameters.contains(key)) {
				continue;
			}
			out.put(key, valueMap.get(key));
		}

		return out;
	}

	protected final Map<String, Object> extractUnmanagedParameters(final Map<String, Object> valueMap,
			final String... managedParameters) {
		final Set<String> parameters = new HashSet<String>();
		for (final String s : managedParameters) {
			parameters.add(s);
		}
		return extractUnmanagedParameters(valueMap, parameters);
	}

	protected final Map<String, String> extractUnmanagedStringParameters(final Map<String, Object> valueMap,
			final Set<String> managedParameters) {
		final Map<String, Object> rawParameters = extractUnmanagedParameters(valueMap, managedParameters);
		final Map<String, String> stringParameters = new HashMap<String, String>();
		for (final Map.Entry<String, Object> rawEntry : rawParameters.entrySet()) {
			stringParameters.put(rawEntry.getKey(), readString(rawEntry.getValue()));
		}
		return stringParameters;
	}

	protected final Map<String, String> extractUnmanagedStringParameters(final Map<String, Object> valueMap,
			final String... managedParameters) {
		final Set<String> parameters = new HashSet<String>();
		for (final String s : managedParameters) {
			parameters.add(s);
		}
		return extractUnmanagedStringParameters(valueMap, parameters);
	}
}
