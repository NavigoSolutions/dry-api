package com.navigo3.dryapi.core.context;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public interface AppContext {
	boolean getIsDevelopmentInstance();
	
	void destroy();

	void transaction(Supplier<Boolean> block);
	
	void executeAfterCommit(Runnable block);
	
	void reportException(Throwable t);
	
	boolean getIsAuthenticated();
	
	Optional<Set<String>> getAllowedQualifiedNames();
	
	void markSigned();
}
