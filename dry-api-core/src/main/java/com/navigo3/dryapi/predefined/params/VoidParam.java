package com.navigo3.dryapi.predefined.params;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableVoidParam.class)
@JsonDeserialize(as = ImmutableVoidParam.class)
public interface VoidParam {
	static VoidParam create() {
		return ImmutableVoidParam.builder().build();
	}
}