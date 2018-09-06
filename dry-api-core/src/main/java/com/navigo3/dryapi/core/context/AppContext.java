package com.navigo3.dryapi.core.context;

import com.navigo3.dryapi.core.util.ExceptionUtils.SupplierWithException;

public interface AppContext {
	boolean getIsDevelopmentInstance();
	void destroy();
	<T> T transaction(SupplierWithException<T> block);
}
