package com.navigo3.dryapi.core.def;

public interface MethodInterface<TInput, TOutput> {
	String getQualifiedName();

	String getDescription();

	IOTypeReference<TInput> getInputType();

	IOTypeReference<TOutput> getOutputType();
}
