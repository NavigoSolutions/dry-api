package com.navigo3.dryapi.predefined.params;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableEmptyResponseParam.class)
@JsonDeserialize(as = ImmutableEmptyResponseParam.class)
public interface EmptyResponseParam {
	static EmptyResponseParam create() {
		return ImmutableEmptyResponseParam.builder().build();
	}
}