package com.navigo3.dryapi.sample.impls;

import com.navigo3.dryapi.core.context.AppContext;

public class TestAppContext implements AppContext {
	
	private boolean isDevelopmentInstance;

	public TestAppContext(boolean isDevelopmentInstance) {
		this.isDevelopmentInstance = isDevelopmentInstance;
	}
	
	@Override
	public boolean getIsDevelopmentInstance() {
		return isDevelopmentInstance;
	}

}
