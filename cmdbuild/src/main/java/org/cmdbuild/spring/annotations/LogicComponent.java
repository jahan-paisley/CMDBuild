package org.cmdbuild.spring.annotations;

import static org.cmdbuild.spring.util.Constants.PROTOTYPE;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Scope(PROTOTYPE)
@Lazy
public @interface LogicComponent {

	String value() default "";

}
