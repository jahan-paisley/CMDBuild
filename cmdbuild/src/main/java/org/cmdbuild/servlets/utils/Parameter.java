package org.cmdbuild.servlets.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The parameter has to be find in the GET/POST variables.
 * Multiple keys means that a transformer has to be used.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.ANNOTATION_TYPE})
public @interface Parameter {
	String value();
	boolean required() default(true);
	
	/**
	 * The "type" will be used ONLY when constructing a &#64;Composite object,
	 * to be able to determine which type is the object.
	 * By default a String object will be returned.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	Class type() default String.class;
}
