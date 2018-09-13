package com.navigo3.dryapi.sample.defs.form;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.immutables.value.Value;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.IdResult;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;

public class FormUpsertEndpoint extends MethodDefinition<Person, IdResult> {
	@Value.Immutable
	@JsonSerialize(as = ImmutablePerson.class)
	@JsonDeserialize(as = ImmutablePerson.class)
	public interface Person {
		String getName();
		String getSurname();
		int getAge();
		
		int getSecretNumber();

		Map<String, List<Integer>> getColorsToFavoriteNumbers();
		
		public static Person createSampleData() {
			return ImmutablePerson
				.builder()
				.name("Jarek")
				.surname("Kuboš")
				.age(42)
				.secretNumber(13)
				.putColorsToFavoriteNumbers("red", Arrays.asList(1, 5, 7))
				.putColorsToFavoriteNumbers("blue", Arrays.asList(42, 74))
				.build();
		}
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableIdResult.class)
	@JsonDeserialize(as = ImmutableIdResult.class)
	public interface IdResult {
		int getId();
	}

	@Override
	public TypeReference<Person> getInputType() {
		return new TypeReference<Person>() {};
	}

	@Override
	public TypeReference<IdResult> getOutputType() {
		return new TypeReference<IdResult>() {};
	}

	@Override
	public String getQualifiedName() {
		return "form/upsert";
	}

	@Override
	public String getDescription() {
		return "Insert or update person record and retur its ID. Only mock implementation actually not saving anything!";
	}
}
