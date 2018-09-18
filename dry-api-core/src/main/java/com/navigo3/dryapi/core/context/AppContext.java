package com.navigo3.dryapi.core.context;

public interface AppContext {
	boolean getIsDevelopmentInstance();
	void destroy();

	void transaction(Runnable block);
	
	void executeAfterCommit(Runnable block);
	
	void reportException(Throwable t);
}
