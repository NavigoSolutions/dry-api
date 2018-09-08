package com.navigo3.dryapi.sample.impls.form;

import java.util.Optional;

import com.navigo3.dryapi.core.impl.ImmutableMethodSecurity.Builder;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.path.TypePathBuilder;
import com.navigo3.dryapi.core.security.logic.False;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.IdResult;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;
import com.navigo3.dryapi.sample.defs.form.ImmutableIdResult;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestAppValidator;
import com.navigo3.dryapi.sample.impls.TestCallContext;

public class FormUpsertImpl extends MethodImplementation<Person, IdResult, TestAppContext, TestCallContext> {
	
	@Override
	public void defineClassSecurity(Builder<TestAppContext, TestCallContext> security) {
		security.authorization(new True<>());
		security.inputFieldsSecurity(buildInputFieldsSecurity(fieldSecurity->{
			fieldSecurity.add(TypePathBuilder.of(x->x.field("secretNumber")), new False<>());
			fieldSecurity.add(TypePathBuilder.of(x->x.field("colorsToFavoriteNumbers").key().index()), new False<>());
		}));
	}
	
	@Override
	public TestCallContext prepareCallContext(Person input) {
		return new TestCallContext();
	}

	@Override
	public Optional<ValidationData> validate(Person input) {
		return TestAppValidator.build(builder->{
			builder.checkNotBlank(inputPath("name"), input.getName());
			builder.checkNotBlank(inputPath("surname"), input.getSurname());
			
			if (input.getAge()<18) {
				builder.addValidationItem(Severity.WARNING, inputPath("age"), "You should be of age 18 or more");
			}
		});
	}

	@Override
	public IdResult execute(Person input) {
		return ImmutableIdResult.builder().id(737).build();
	}
}
