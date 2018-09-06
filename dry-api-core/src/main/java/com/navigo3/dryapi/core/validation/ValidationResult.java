package com.navigo3.dryapi.core.validation;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;

@Value.Immutable
@JsonSerialize(as = ImmutableValidationResult.class)
@JsonDeserialize(as = ImmutableValidationResult.class)
@JsonIgnoreProperties(value={ "overallSuccess" }, allowGetters=true)
public interface ValidationResult {
	List<ValidationItem> getItems();
	
	default boolean getOverallSuccess() {
		return getItems().stream().noneMatch(r->r.getSeverity()==Severity.error);
	}
}
