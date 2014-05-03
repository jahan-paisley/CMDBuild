package org.cmdbuild.logic.auth;

public interface ModelValidator<T> {

	/**
	 * Method for validating the model (a simple DTO)
	 * 
	 * @param model
	 * @return true if the model is ok, false otherwise.
	 */
	public boolean validate(T model);

}
