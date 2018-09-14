package com.navigo3.dryapi.core.exec.json;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableJsonRequest.class)
@JsonDeserialize(as = ImmutableJsonRequest.class)
public interface JsonRequest {
	public enum RequestType {
		VALIDATE,
		EXECUTE,
		INPUT_FIELDS_SECURITY
	}
	
	String getRequestUuid();
	String getQualifiedName();
	RequestType getRequestType();
	
	JsonNode getInput();
	
	List<InputOutputMapping> getInputMappings();
}
