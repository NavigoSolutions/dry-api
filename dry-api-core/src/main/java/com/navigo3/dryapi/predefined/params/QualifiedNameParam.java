package com.navigo3.dryapi.predefined.params;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Value.Immutable
@JsonSerialize(as = ImmutableQualifiedNameParam.class)
@JsonDeserialize(as = ImmutableQualifiedNameParam.class)
public interface QualifiedNameParam {
	String getQualifiedName();

	static QualifiedNameParam create(String qualifiedName) {
		return ImmutableQualifiedNameParam.builder().qualifiedName(qualifiedName).build();
	}
}