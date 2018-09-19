package com.navigo3.dryapi.core.util;

public class LambdaUtils {
	@FunctionalInterface
	public interface RunnableWithException {
		void run() throws Throwable;
	}
	
	@FunctionalInterface
	public interface SupplierWithException<T> {
		T get() throws Throwable;
	}
	
	@FunctionalInterface
	public interface ConsumerWithException<T> {
		void accept(T param) throws Throwable;
	}
	
	@FunctionalInterface
	public interface FunctionWithException<T, U> {
		U apply(T param) throws Throwable;
	}
}
