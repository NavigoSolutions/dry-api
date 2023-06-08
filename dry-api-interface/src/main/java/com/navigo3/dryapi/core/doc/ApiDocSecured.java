package com.navigo3.dryapi.core.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.navigo3.dryapi.core.utils.Nullable;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Nullable
public @interface ApiDocSecured {
	String value() default "EXTRA RIGHTS REQUIRED";
}
