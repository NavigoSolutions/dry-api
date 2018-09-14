package com.navigo3.dryapi.sample.impls;

import java.util.ArrayList;
import java.util.List;

import com.navigo3.dryapi.core.context.AppContext;

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
	public void transaction(Runnable block) {
		block.run();
		
		runAfterCommit.forEach(Runnable::run);
	}
	
	@Override
	public void executeAfterCommit(Runnable block) {
		runAfterCommit.add(block);
	}
}
