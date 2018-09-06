package com.navigo3.dryapi.core.impl;

import java.util.Optional;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.ValidationData;

public abstract class MethodImplementation<TInput, TOutput, TContext extends AppContext, TCallContext extends CallContext> {

	private boolean initialized = false;
	
	private ExecutionContext<TContext, TCallContext> executionContext;
	
	public abstract SecurityCheck<TContext, TCallContext> getAuthorization();
	
	public abstract TCallContext prepareCallContext(TInput input);
	
	public abstract Optional<ValidationData> validate(TInput input);
	
	public abstract TOutput execute(TInput input);

	public void initialize(ExecutionContext<TContext, TCallContext> executionContext) {
		Validate.isFalse(initialized, "Already initialized!");
		
		Validate.notNull(executionContext);
		
		this.executionContext = executionContext;
		
		initialized  = true;
	}

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
	
	public StructurePath inputPath(Object... items) {
		return executionContext.getInputPathsTree().buildPath(items);
	}
	
	public StructurePath outputPath(Object... items) {
		return executionContext.getOutputPathsTree().buildPath(items);
	}
}
