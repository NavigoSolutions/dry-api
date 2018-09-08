package com.navigo3.dryapi.sample.impls.list;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.navigo3.dryapi.core.impl.ImmutableMethodSecurity.Builder;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;
import com.navigo3.dryapi.sample.defs.form.ImmutablePerson;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;

public class ListPersonsImpl extends MethodImplementation<Map<String, List<Person>>, List<Person>, TestAppContext, TestCallContext> {

	@Override
	public void defineClassSecurity(Builder<TestAppContext, TestCallContext> builder) {
		builder.authorization(new True<>());
	}

	@Override
	public TestCallContext prepareCallContext(Map<String, List<Person>> input) {
		return new TestCallContext();
	}

	@Override
	public Optional<ValidationData> validate(Map<String, List<Person>> input) {
		return Optional.empty();
	}

	@Override
	public List<Person> execute(Map<String, List<Person>> input) {
		return Arrays.asList(
			ImmutablePerson.builder().name("One").surname("One").age(1).secretNumber(42).putColorsToFavoriteNumbers("red", Arrays.asList(1, 2, 3)).build(),
			ImmutablePerson.builder().name("Two").surname("Two").age(2).secretNumber(666).putColorsToFavoriteNumbers("blue", Arrays.asList(7, 13)).build()
		);
	}

}
