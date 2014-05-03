/**
 * 
 */
package org.cmdbuild.servlets.utils;

import javax.servlet.http.HttpServletRequest;

public interface ParameterBuilder<T> {
	T build(HttpServletRequest r, OverrideKeys overrides) throws Exception;
	Class<T> getBindedClass();
}