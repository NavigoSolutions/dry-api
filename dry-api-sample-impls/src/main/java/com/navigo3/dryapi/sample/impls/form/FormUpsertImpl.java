package com.navigo3.dryapi.sample.impls.form;

import java.util.Optional;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.path.StructurePathBuilder;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.security.logic.Everyone;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;
import com.navigo3.dryapi.core.validation.ValidationResult;
import com.navigo3.dryapi.core.validation.ValidationResultBuilder;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.IdResult;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;
import com.navigo3.dryapi.sample.defs.form.ImmutableIdResult;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;

public class FormUpsertImpl extends MethodImplementation<Person, IdResult, TestAppContext, TestCallContext> {

	@Override
	public SecurityCheck<TestAppContext, TestCallContext> getAuthorization() {
		return new Everyone<>();
	}

	@Override
	public TestCallContext prepareCallContext(Person input) {
		return new TestCallContext();
	}

	@Override
	public Optional<ValidationResult> validate(Person input) {
		ValidationResultBuilder builder = ValidationResultBuilder.create();
		
		builder.checkNotBlank(StructurePathBuilder.create().key("name").build(), input.getName());
		builder.checkNotBlank(StructurePathBuilder.create().key("surname").build(), input.getSurname());
		
		if (input.getAge()<18) {
			builder.addValidationItem(Severity.warning, StructurePathBuilder.create().key("age").build(), "You should be of age 18 or more");
		}
		
		return Optional.of(builder.build());
	}

	@Override
	public IdResult execute(Person input) {
		return ImmutableIdResult.builder().id(737).build();
	}
}
