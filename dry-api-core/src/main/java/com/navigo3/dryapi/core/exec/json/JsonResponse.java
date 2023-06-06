package com.navigo3.dryapi.core.exec.json;

import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.exec.ResponseStatus;
import com.navigo3.dryapi.core.exec.json.JsonRequest.RequestType;
import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.validation.ValidationData;

@Value.Immutable
@JsonSerialize(as = ImmutableJsonResponse.class)
@JsonDeserialize(as = ImmutableJsonResponse.class)
public interface JsonResponse {
	String getRequestUuid();

	RequestType getRequestType();

	String getQualifiedName();

	ResponseStatus getStatus();

	Optional<JsonNode> getOutput();

	Optional<ValidationData> getValidation();

	Optional<String> getErrorMessage();

	Optional<ObjectPathsTree> getAllowedInputFields();

	Optional<ObjectPathsTree> getAllowedOutputFields();
}