package com.navigo3.dryapi.core.impl;

import java.util.Optional;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.util.StringUtils;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.ImmutableValidationData;
import com.navigo3.dryapi.core.validation.ValidationData;

public abstract class MethodImplementation<TInput, TOutput, TAppContext extends AppContext, TCallContext extends CallContext> {

	private boolean initialized = false;
	
	private ExecutionContext<TAppContext, TCallContext> executionContext;

	private MethodDefinition<TInput, TOutput> definition;

	private MethodSecurity<TAppContext, TCallContext> security;
	
	/**
	 * Abstract methods for client implementation - security handling
	 */
	
	public abstract void fillClassSecurity(MethodSecurityBuilder<TAppContext, TCallContext> securityBuilder);
	
	/**
	 * Abstract methods for client implementation - request handling
	 */
	
	public abstract TCallContext prepareCallContext(TInput input);
	
	public abstract Optional<ValidationData> validate(TInput input);
	
	public abstract TOutput execute(TInput input);
	
	/**
	 * Methods usable during request handling
	 */
	
	protected MethodDefinition<TInput, TOutput> getDefinition() {
		Validate.notNull(definition);
		
		return definition;
	}
	
	protected MethodSecurity<TAppContext, TCallContext> getSecurity() {
		Validate.notNull(security);
		
		return security;
	}
	
	protected TAppContext getAppContext() {
		Validate.isTrue(initialized, "Not yet initialized!");
		Validate.notNull(executionContext.getAppContext());
		
		return executionContext.getAppContext();
	}

	protected TCallContext getCallContext() {
		Validate.isTrue(initialized, "Not yet initialized!");
		Validate.isTrue(executionContext.getCallContext().isPresent(), "Call context not yet prepared");
		
		return executionContext.getCallContext().get();
	}
	
	/**
	 * Private stuff
	 */

	public void initialize(MethodDefinition<TInput, TOutput> definition, ExecutionContext<TAppContext, TCallContext> executionContext) {
		Validate.isFalse(initialized, "Already initialized!");
		
		Validate.notNull(definition);
		Validate.notNull(executionContext);
		
		this.definition = definition;		
		this.executionContext = executionContext;
		
		initialized  = true;
	}

	public void setDefinition(MethodDefinition<TInput, TOutput> definition) {
		Validate.notNull(definition);
		
		this.definition = definition;
	}

	public void setSecurity(MethodSecurity<TAppContext, TCallContext> security) {
		Validate.notNull(security);
		
		this.security = security;
	}

	public ValidationData securedValidate(TInput input, ObjectPathsTree tree) {
		Optional<ValidationData> res = validate(input);
		
		Validate.notNull(res);
		
		res.ifPresent(validatorData->{
			validatorData.getItems().forEach(item->{
				try {
					tree.throwIfPathDoesNotExists(item.getPath());
				} catch (Throwable t) {
					throw new RuntimeException(
						StringUtils.subst("There is no path [{}] at {}.validate()", item.getPath().toDebug(), getClass().getName()), 
						t
					);
				}	
			});
		});
		
		return res.orElse(ImmutableValidationData.builder().build());
	}
}
