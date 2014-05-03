package org.cmdbuild.workflow;

import static java.util.Collections.unmodifiableMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.contains;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trimToNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;

public abstract class ActivityPerformerExpressionEvaluator {

	private static final String PERFORMERS_SEPARATOR = ",";

	private static final Map<String, Object> MISSING_VARIABLES = Collections.<String, Object> emptyMap();

	protected final String expression;
	protected Map<String, Object> variables;

	public ActivityPerformerExpressionEvaluator(final String expression) {
		this.expression = expression;
		this.variables = MISSING_VARIABLES;
	}

	public void setVariables(final Map<String, Object> variables) {
		this.variables = (variables == null) ? MISSING_VARIABLES : unmodifiableMap(variables);
	}

	/**
	 * Returns the names contained in the expression.
	 * 
	 * @return the names.
	 */
	public Set<String> getNames() {
		final String evaluated = safeEvaluate();
		return extractNames(evaluated);
	}

	/**
	 * Extracts names from specified string.
	 * 
	 * @param s
	 *            is the string containing the names.
	 * 
	 * @return the extracted names.
	 */
	private Set<String> extractNames(final String s) {
		final Set<String> names = Sets.newHashSet();
		if (contains(s, PERFORMERS_SEPARATOR)) {
			for (final String name : split(s, PERFORMERS_SEPARATOR)) {
				final String trimmedName = trimToNull(name);
				if (trimmedName != null) {
					names.add(trimmedName);
				}
			}
		} else if (isNotBlank(s)) {
			names.add(s);
		}
		return names;
	}

	/**
	 * Safe evaluates expression.
	 * 
	 * @return the evaluated expression or an empty string on error.
	 */
	private String safeEvaluate() {
		try {
			return evaluate();
		} catch (final Exception e) {
			return EMPTY;
		}
	}

	/**
	 * Evaluates the expression.
	 * 
	 * @return the evaluated expression.
	 * 
	 * @throws Exception
	 *             if an error occurs during evaluation.
	 */
	protected abstract String evaluate() throws Exception;

}