package com.navigo3.dryapi.sample.impls;

import java.util.ArrayList;
import java.util.List;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.util.ExceptionUtils;
import com.navigo3.dryapi.core.util.LambdaUtils.SupplierWithException;

public class TestAppContext implements AppContext {
	
	private boolean isDevelopmentInstance;
	
	private List<Runnable> runAfterCommit = new ArrayList<>();

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
		T res = ExceptionUtils.withRuntimeException(block);
		
		runAfterCommit.forEach(Runnable::run);
		
		return res;
	}
	
	@Override
	public void executeAfterCommit(Runnable block) {
		runAfterCommit.add(block);
	}
}
