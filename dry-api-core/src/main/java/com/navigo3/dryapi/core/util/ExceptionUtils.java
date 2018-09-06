package com.navigo3.dryapi.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionUtils {
	
	public interface RunnableWithException {
		public void run() throws Throwable;
	}
	
	public interface SupplierWithException<T> {
		public T get() throws Throwable;
	}
	
	public interface FunctionWithException<T, U> {
		public U apply(T param) throws Throwable;
	}
	
	public static String extractStacktrace(String message, Throwable t) {
		StringWriter w = new StringWriter();
		PrintWriter pw = new PrintWriter(w);
		
		pw.write(message);
		w.write("\n");
		t.printStackTrace(pw);
		
		return w.toString();
	}
	
	public static void withRuntimeException(RunnableWithException block) {
		try {
			block.run();
		} catch (Throwable t) {
			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} else {
				throw new RuntimeException(t);
			}
		}
	}
	
	public static <T> T withRuntimeException(SupplierWithException<T> block) {
		try {
			return block.get();
		} catch (Throwable t) {
			if (t instanceof RuntimeException) {
				throw (RuntimeException)t;
			} else {
				throw new RuntimeException(t);
			}
		}
	}
}
