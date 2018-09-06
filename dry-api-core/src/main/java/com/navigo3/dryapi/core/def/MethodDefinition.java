package com.navigo3.dryapi.core.def;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.util.Validate;

public abstract class MethodDefinition<TInput, TOutput> {
	
	private TypeSchema inputSchema;
	private TypeSchema outputSchema;
	
	private boolean initialized = false;
	
	public void initialize() {
		Validate.isFalse(initialized);
		
		this.inputSchema = TypeSchema.build(getInputType());
		this.outputSchema = TypeSchema.build(getOutputType());
		
		initialized = true;
	}
	
	public TypeSchema getInputSchema() {
		Validate.isTrue(initialized);
		
		return inputSchema;
	}
	
	public TypeSchema getOutputSchema() {
		Validate.isTrue(initialized);
		
		return outputSchema;
	}

	public boolean isInitialized() {
		return initialized;
	}

	public abstract TypeReference<TInput> getInputType();
	
	public abstract TypeReference<TOutput> getOutputType();
	
	public abstract String getQualifiedName();
	
	public abstract String getDescription();
}
