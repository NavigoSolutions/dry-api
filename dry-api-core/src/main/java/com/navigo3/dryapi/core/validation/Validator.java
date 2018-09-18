package com.navigo3.dryapi.core.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;

public abstract class Validator {
	private final ObjectPathsTree allowedPaths;
	private List<ValidationItem> items = new ArrayList<>();

	public Validator(ObjectPathsTree allowedPaths) {
		this.allowedPaths = allowedPaths;
	}
	
	public void addItem(Severity severity, StructurePath path, String message) {
		allowedPaths.throwIfPathDoesNotExists(path);
		
		items.add(ImmutableValidationItem
			.builder()
			.severity(severity)
			.path(path)
			.message(message)
			.build()
		);
	}
	
	public void addAll(Collection<ValidationItem> coll) {
		items.addAll(coll);
	}

	public ValidationData build() {
		return ImmutableValidationData.builder().items(items).build();
	}
}