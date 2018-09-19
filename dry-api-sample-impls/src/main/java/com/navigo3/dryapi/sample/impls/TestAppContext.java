package com.navigo3.dryapi.sample.impls;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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

	@Override
	public void reportException(Throwable t) {
		//
	}

	@Override
	public Optional<Set<String>> getAllowedQualifiedNames() {
		return Optional.empty();
	}

	@Override
	public Optional<Set<InetAddress>> getAllowedIpAddresses() {
//		return ExceptionUtils.withRuntimeException(()->Optional.of(new HashSet<>(Arrays.asList(InetAddress.getByName("192.168.0.1")))));
//		return ExceptionUtils.withRuntimeException(()->Optional.of(new HashSet<>(Arrays.asList(InetAddress.getByName("127.0.0.1")))));
		return Optional.empty();
	}

	@Override
	public boolean getIsAuthenticated() {
		return true;
//		return false;
	}
}
