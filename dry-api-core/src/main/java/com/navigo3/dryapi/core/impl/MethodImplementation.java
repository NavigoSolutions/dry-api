package com.navigo3.dryapi.core.impl;

import java.util.Optional;
import java.util.function.Consumer;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.field.FieldsSecurity;
import com.navigo3.dryapi.core.security.field.FieldsSecurityBuilder;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.ValidationData;

public abstract class MethodImplementation<TInput, TOutput, TAppContext extends AppContext, TCallContext extends CallContext> {

	private boolean initialized = false;
	
	private ExecutionContext<TAppContext, TCallContext> executionContext;

	private MethodDefinition<TInput, TOutput> definition;
	
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
	
	protected StructurePath inputPath(Object... items) {
		return executionContext.getInputPathsTree().buildPath(items);
	}
	
	protected StructurePath outputPath(Object... items) {
		return executionContext.getOutputPathsTree().buildPath(items);
	}
	
	protected FieldsSecurity<TAppContext, TCallContext> buildInputFieldsSecurity(Consumer<FieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		return FieldsSecurityBuilder.build(getDefinition().getInputSchema(), block);
	}
	
	protected FieldsSecurity<TAppContext, TCallContext> buildOutputFieldsSecurity(Consumer<FieldsSecurityBuilder<TAppContext, TCallContext>> block) {
		return FieldsSecurityBuilder.build(getDefinition().getOutputSchema(), block);
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
}
