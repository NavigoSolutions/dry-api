package com.navigo3.dryapi.core.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;

public class ValidationResultBuilder {
	
	private List<ValidationItem> items = new ArrayList<>();

	public static ValidationResultBuilder create() {
		return new ValidationResultBuilder();
	}
	
	public ValidationResult build() {
		return ImmutableValidationResult.builder().items(items).build();
	}

	public void checkNotNull(StructurePath path, Object val) {
		if (val==null) {
			addValidationItem(Severity.error, path, "Field should not be empty!");
		}
	}	
	
	public void checkNotBlank(StructurePath path, String val) {
		if (val==null || val.trim().isEmpty()) {
			addValidationItem(Severity.error, path, "Field should not be blank!");
		}
	}

	public void checkPresent(StructurePath path, Optional<?> val) {
		if (!val.isPresent()) {
			addValidationItem(Severity.error, path, "Field should not be empty!");
		}
	}

	public void addValidationItem(Severity severity, StructurePath path, String message) {
		items.add(ImmutableValidationItem
			.builder()
			.severity(severity)
			.path(path)
			.message(message)
			.build()
		);
	}
}
