package org.cmdbuild.servlets.utils;

import javax.servlet.http.HttpServletRequest;

/**
 * Interface for string(s) transformers, used for create the correct instances of objects to be used in --as for now--
 * json method calls.
 * @see org.cmdbuild.servlets.utils.transformer.AbstractTransformer
 * @param <T>
 */
public interface Transformer<T> {
	/**
	 * Return an instance of the class T
	 * @param request
	 * @param response
	 * @param context some object, currently used only for value of length > 1: the JSONDispatcher sends the String[] key array
	 * @param value one or more strings that holds the string representation on the output value
	 * @return an instance of an object
	 * @throws Exception
	 */
	T transform( HttpServletRequest request, Object context,String...value ) throws Exception;
	
	/**
	 * The T class default value. Possibly null.
	 * @return
	 */
	T defaultValue();
	
	/**
	 * Return the class of the transformed object
	 * @return
	 */
	Class<T> getTransformedClass();
}
