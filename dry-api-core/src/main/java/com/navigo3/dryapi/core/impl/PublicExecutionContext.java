package com.navigo3.dryapi.core.impl;

import java.util.Optional;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;

public interface PublicExecutionContext<TAppContext extends AppContext, TCallContext extends CallContext> {
	TAppContext getAppContext();

	Optional<TCallContext> getCallContext();
}
