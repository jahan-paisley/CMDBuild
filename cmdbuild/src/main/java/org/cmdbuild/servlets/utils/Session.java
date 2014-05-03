package org.cmdbuild.servlets.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The parameter has to be found in the requests' session.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER,ElementType.ANNOTATION_TYPE})
public @interface Session {
	String value();
	boolean required() default(true);

}
