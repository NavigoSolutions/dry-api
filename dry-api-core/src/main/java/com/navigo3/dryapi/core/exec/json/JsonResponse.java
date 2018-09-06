package com.navigo3.dryapi.core.exec.json;

import java.util.Optional;
import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.exec.ResponseStatus;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.validation.ValidationResult;

@Value.Immutable
@JsonSerialize(as = ImmutableJsonResponse.class)
@JsonDeserialize(as = ImmutableJsonResponse.class)
public interface JsonResponse {
	UUID getRequestUuid();
	
	ResponseStatus getStatus();
	
	Optional<String> getOutputJson();
	
	Optional<ValidationResult> getValidation();
	
	Optional<String> getErrorMessage();
	
	Optional<ObjectPathsTree> getAllowedInputFields();
	
	Optional<ObjectPathsTree> getAllowedOutputFields();
}