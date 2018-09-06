package com.navigo3.dryapi.core.exec.json;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableJsonBatchRequest.class)
@JsonDeserialize(as = ImmutableJsonBatchRequest.class)
public interface JsonBatchRequest {
	List<JsonRequest> getRequests();
}
