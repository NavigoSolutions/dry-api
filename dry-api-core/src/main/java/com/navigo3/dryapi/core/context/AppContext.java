package com.navigo3.dryapi.core.context;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public interface AppContext {
	void start(List<String> endpoints);

	boolean getIsDevelopmentInstance();

	void destroy();

	void transaction(Supplier<Boolean> block);

	void reportException(Throwable t);

	boolean getIsAuthenticated();

	Optional<Set<String>> getAllowedQualifiedNames();
}
