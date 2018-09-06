package com.navigo3.dryapi.sample.impls;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.LambdaUtils.SupplierWithException;

public class TestAppContext implements AppContext {
	
	private boolean isDevelopmentInstance;

	public TestAppContext(boolean isDevelopmentInstance) {
		this.isDevelopmentInstance = isDevelopmentInstance;
	}
	
	@Override
	public boolean getIsDevelopmentInstance() {
		return isDevelopmentInstance;
	}

	@Override
	public void destroy() {
	}

	@Override
	public <T> T transaction(SupplierWithException<T> block) {
		return ExceptionUtils.withRuntimeException(block);
	}
}
