package com.navigo3.dryapi.sample.impls;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.validation.ImmutableValidationData;
import com.navigo3.dryapi.core.validation.ImmutableValidationItem;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.core.validation.ValidationItem;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;

public class TestAppValidator {
	
	private List<ValidationItem> items = new ArrayList<>();
	
	public static Optional<ValidationData> empty() {
		return Optional.empty();
	}

	public static Optional<ValidationData> build(Consumer<TestAppValidator> block) {
		TestAppValidator builder = new TestAppValidator();
		
		block.accept(builder);
		
		return Optional.of(ImmutableValidationData.builder().items(builder.items).build());
	}
	
	public void checkNotNull(StructurePath path, Object val) {
		if (val==null) {
			addValidationItem(Severity.ERROR, path, "Field should not be empty!");
		}
	}	
	
	public void checkNotBlank(StructurePath path, String val) {
		if (val==null || val.trim().isEmpty()) {
			addValidationItem(Severity.ERROR, path, "Field should not be blank!");
		}
	}

	public void checkPresent(StructurePath path, Optional<?> val) {
		if (!val.isPresent()) {
			addValidationItem(Severity.ERROR, path, "Field should not be empty!");
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
