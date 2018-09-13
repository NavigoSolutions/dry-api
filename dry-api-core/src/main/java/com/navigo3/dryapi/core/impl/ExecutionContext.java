package com.navigo3.dryapi.core.impl;

import java.util.Optional;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.util.Validate;

public class ExecutionContext<TAppContext extends AppContext, TCallContext extends CallContext> implements PublicExecutionContext<TAppContext, TCallContext> {

	private TAppContext appContext;
	private Optional<TCallContext> callContext = Optional.empty();
	
	public ExecutionContext(TAppContext appContext) {
		Validate.notNull(appContext);
		
		this.appContext = appContext;
	}
	
	public void setCallContext(TCallContext callContext) {
		Validate.notNull(callContext);
		Validate.isFalse(this.callContext.isPresent());
		
		this.callContext = Optional.of(callContext);
	}

	@Override
	public TAppContext getAppContext() {
		return appContext;
	}

	@Override
	public Optional<TCallContext> getCallContext() {
		return callContext ;
	}
}
