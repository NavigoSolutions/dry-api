package com.navigo3.dryapi.core.impl;

import java.util.Optional;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.ValidationResult;

public abstract class MethodImplementation<TInput, TOutput, TContext extends AppContext, TCallContext extends CallContext> {

	private boolean initialized = false;
	
	private ExecutionContext<TContext, TCallContext> executionContext;

	public void initialize(ExecutionContext<TContext, TCallContext> executionContext) {
		Validate.isFalse(initialized, "Already initialized!");
		
		Validate.notNull(executionContext);
		
		this.executionContext = executionContext;
		
		initialized  = true;
	}
	
	public abstract SecurityCheck<TContext, TCallContext> getAuthorization();
	
	public abstract TCallContext prepareCallContext(TInput input);
	
	public abstract Optional<ValidationResult> validate(TInput input);
	
	public abstract TOutput execute(TInput input);

	public TContext getAppContext() {
		Validate.isTrue(initialized, "Not yet initialized!");
		Validate.notNull(executionContext.getAppContext());
		
		return executionContext.getAppContext();
	}

	public TCallContext getCallContext() {
		Validate.isTrue(initialized, "Not yet initialized!");
		Validate.isTrue(executionContext.getCallContext().isPresent(), "Call context not yet prepared");
		
		return executionContext.getCallContext().get();
	}
}
