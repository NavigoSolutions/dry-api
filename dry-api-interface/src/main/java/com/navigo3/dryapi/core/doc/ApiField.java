package com.navigo3.dryapi.core.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ApiField {

	// Base documentation

	String extraSecurity() default "";

	String description() default "";

	String defaultValue() default "";

	boolean deprecated() default false;

	// String-specific

	int minLength() default -1;

	int maxLength() default -1;

	String pattern() default "";

	String format() default ""; // e.g. email, uuid, date

	// Numeric

	double min() default Double.NaN;

	double max() default Double.NaN;

	// Enum-like

	String[] allowedValues() default {};

}