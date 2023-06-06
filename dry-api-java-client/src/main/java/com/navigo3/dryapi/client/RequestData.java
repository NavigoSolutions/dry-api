package com.navigo3.dryapi.client;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.exec.json.InputOutputMapping;
import com.navigo3.dryapi.core.exec.json.JsonRequest.RequestType;
import com.navigo3.dryapi.core.exec.json.JsonResponse;

@Value.Modifiable
public interface RequestData<TInput, TOutput> {
	MethodDefinition<TInput, TOutput> getMethod();

	String getUuid();

	TInput getInput();

	RequestType getRequestType();

	List<InputOutputMapping> getInputOutputMappings();

	Optional<TOutput> getOutput();

	Optional<JsonResponse> getResponse();
}
