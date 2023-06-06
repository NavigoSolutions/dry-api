package com.navigo3.dryapi.sample.impls;

import java.util.Optional;

import com.navigo3.dryapi.core.meta.ObjectPathsTree;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;
import com.navigo3.dryapi.core.validation.Validator;

public class TestValidator extends Validator {
	public TestValidator(ObjectPathsTree allowedPaths) {
		super(allowedPaths);
		// TODO Auto-generated constructor stub
	}

	public void checkNotNull(StructurePath path, Object val) {
		if (val == null) {
			addItem(Severity.ERROR, path, "Field should not be empty!");
		}
	}

	public void checkNotBlank(StructurePath path, String val) {
		if (val == null || val.trim().isEmpty()) {
			addItem(Severity.ERROR, path, "Field should not be blank!");
		}
	}

	public void checkPresent(StructurePath path, Optional<?> val) {
		if (!val.isPresent()) {
			addItem(Severity.ERROR, path, "Field should not be empty!");
		}
	}
}
