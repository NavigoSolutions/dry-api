package com.navigo3.dryapi.core.exec.json;

import java.util.UUID;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableJsonRequest.class)
@JsonDeserialize(as = ImmutableJsonRequest.class)
public interface JsonRequest {
	public enum RequestType {
		validate,
		execute
	}
	
	UUID getRequestUuid();
	String getQualifiedName();
	RequestType getRequestType();
	
	String getInputJson();
}
