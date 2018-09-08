package com.navigo3.dryapi.core.validation;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.path.StructurePath;

@Value.Immutable
@JsonSerialize(as = ImmutableValidationItem.class)
@JsonDeserialize(as = ImmutableValidationItem.class)
public interface ValidationItem {
	public enum Severity {
		ERROR,
		WARNING
	}
	
	Severity getSeverity();
	String getMessage();
	StructurePath getPath();
}
