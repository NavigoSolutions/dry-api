package com.navigo3.dryapi.core.security.core;

import java.util.List;

public interface ParametrizedSecurityCheck<T> {
	List<T> getUsedContextParams();
	
	List<T> getOptionalContextParams();
}
