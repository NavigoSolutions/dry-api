package com.navigo3.dryapi.core.impl;

import java.util.Optional;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;

public interface PublicExecutionContext<TContext extends AppContext, TCallContext extends CallContext> {
	TContext getAppContext();

	Optional<TCallContext> getCallContext();
}
