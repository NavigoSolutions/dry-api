package com.navigo3.dryapi.core.exec.json;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.exec.ResponseStatus;

@Value.Immutable
@JsonSerialize(as = ImmutableJsonBatchResponse.class)
@JsonDeserialize(as = ImmutableJsonBatchResponse.class)
@JsonIgnoreProperties(value={ "overallSuccess" }, allowGetters=true)
public interface JsonBatchResponse {
	List<JsonResponse> getResponses();
	
	default boolean getOverallSuccess() {
		return getResponses().stream().allMatch(r->r.getStatus()==ResponseStatus.success);
	}
}
