package org.cmdbuild.workflow;

import java.util.Map;

public class BshActivityPerformerExpressionEvaluator extends ActivityPerformerExpressionEvaluator {

	public BshActivityPerformerExpressionEvaluator(final String expression) {
		super(expression);
	}

	@Override
	protected String evaluate() throws Exception {
		final bsh.Interpreter interpreter = new bsh.Interpreter();
		for (final Map.Entry<String, Object> entry : variables.entrySet()) {
			interpreter.set(entry.getKey(), entry.getValue());
		}
		return interpreter.eval(expression).toString();
	}

}