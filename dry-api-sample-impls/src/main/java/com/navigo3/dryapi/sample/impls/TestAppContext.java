package com.navigo3.dryapi.sample.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import com.navigo3.dryapi.core.context.AppContext;

public class TestAppContext implements AppContext {
	
	private boolean isDevelopmentInstance;
	
	private List<Runnable> runAfterCommit = new ArrayList<>();

	@SuppressWarnings("unused")
	private boolean signed;

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
	public void transaction(Supplier<Boolean> block) {
		boolean commit = block.get();
		
		System.out.println("Transaction: "+(commit ? "COMMIT" : "ROLLBACK"));
		
		runAfterCommit.forEach(Runnable::run);
	}

	public void executeAfterCommit(Runnable block) {
		runAfterCommit.add(block);
	}

	@Override
	public void reportException(Throwable t) {
		//
	}

	@Override
	public Optional<Set<String>> getAllowedQualifiedNames() {
		return Optional.empty();
	}

	@Override
	public boolean getIsAuthenticated() {
		return true;
//		return false;
	}

	@Override
	public void markSigned() {
		signed = true;
	}
}
