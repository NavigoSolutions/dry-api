package com.navigo3.dryapi.core.def;

import java.lang.reflect.Type;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.util.Validate;

public class MethodDefinition<TInput, TOutput>{
	
	private TypeSchema inputSchema;
	private TypeSchema outputSchema;
	
	private boolean initialized = false;
	
	private MethodInterface<TInput, TOutput> interf;
	
	private TypeReference<TInput> inputType;
	
	private TypeReference<TOutput> outputType;
	
	public MethodDefinition(MethodInterface<TInput, TOutput> interf) {
		this.interf = interf;
		
		this.inputType = new TypeReference<TInput>() {
			@Override
			public Type getType() {
				return interf.getInputType().getType();
			}
		};
		
		this.outputType = new TypeReference<TOutput>() {
			@Override
			public Type getType() {
				return interf.getOutputType().getType();
			}
		};;
	}
	
	void initialize() {
		Validate.isFalse(initialized);
		
		inputSchema = TypeSchema.build(getInputType());
		outputSchema = TypeSchema.build(getOutputType());
		
//		JsonUtils.prettyPrint(inputSchema);
//		JsonUtils.prettyPrint(outputSchema);
		
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
	
	public String getQualifiedName() {
		return interf.getQualifiedName();
	}
	
	public String getDescription() {
		return interf.getDescription();
	}
	
	public TypeReference<TInput> getInputType() {
		return inputType;
	}
	
	public TypeReference<TOutput> getOutputType() {
		return outputType;
	}
}
