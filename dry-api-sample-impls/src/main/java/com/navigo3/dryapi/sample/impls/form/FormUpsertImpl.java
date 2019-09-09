package com.navigo3.dryapi.sample.impls.form;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodMetadataBuilder;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.security.logic.False;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.core.validation.ValidationItem.Severity;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.IdResult;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;
import com.navigo3.dryapi.sample.defs.form.ImmutableIdResult;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;

public class FormUpsertImpl extends MethodImplementation<Person, IdResult, FormUpsertEndpoint, TestAppContext, TestCallContext, TestValidator> {
	
	@Override
	public void fillClassMetadata(MethodMetadataBuilder<TestAppContext, TestCallContext> metadata) {
	}
	
	@Override
	public void fillClassSecurity(MethodSecurityBuilder<TestAppContext, TestCallContext> security) {
		security.authorization(new True<>());
		security.defineInputFieldsTypeSecurity(builder->{
			builder.add(TypePath.field("name"), new True<>());
			builder.add(TypePath.field("surname"), new True<>());
			builder.add(TypePath.field("age"), new True<>());
			
			builder.add(TypePath.field("secretNumber"), new False<>());
			builder.add(TypePath.field("colorsToFavoriteNumbers").addKey().addIndex(), new False<>());
		});
	}
	
	@Override
	public TestCallContext prepareCallContext(Person input) {
		return new TestCallContext();
	}

	@Override
	public void validate(Person input, TestValidator validator) {
		validator.checkNotBlank(StructurePath.key("name"), input.getName());
		validator.checkNotBlank(StructurePath.key("surname"), input.getSurname());
		
		if (input.getAge()<18) {
			validator.addItem(Severity.WARNING, StructurePath.key("age"), "You should be of age 18 or more");
		}
	}

	@Override
	public IdResult execute(Person input) {
		return ImmutableIdResult.builder().id(737).build();
	}
}
