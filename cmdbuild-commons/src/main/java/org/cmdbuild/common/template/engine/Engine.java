package org.cmdbuild.common.template.engine;

public interface Engine {

	/**
	 * Evaluates the specified expression.
	 * 
	 * @param expression
	 * 
	 * @return the result of the evaluation process, {@code null} if evaluation
	 *         was unsuccessful.
	 */
	Object eval(String expression);

}