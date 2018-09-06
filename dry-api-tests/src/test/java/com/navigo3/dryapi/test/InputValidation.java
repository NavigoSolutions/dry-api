package com.navigo3.dryapi.test;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.navigo3.dryapi.core.util.JsonUtils;
import com.navigo3.dryapi.core.validation.ValidationResult;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;
import com.navigo3.dryapi.sample.defs.form.ImmutablePerson;
import com.navigo3.dryapi.test.helpers.RemoteCallsEnvironment;

public class InputValidation {
	private static RemoteCallsEnvironment env = new RemoteCallsEnvironment();

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		env.start();
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		env.stop();
	}
	
	@Test
	public void testAllSeverities() {
		Person input = ImmutablePerson
			.builder()
			.name("James")
			.surname("")
			.age(10)
			.putColorsToFavoriteNumbers("red", Arrays.asList(1, 5, 7))
			.putColorsToFavoriteNumbers("blue", Arrays.asList(42))
			.putColorsToFavoriteNumbers("green", Arrays.asList(2, 10, 15, 78))
			.build();
		
		ValidationResult output = env.getApi().validateBlocking(new FormUpsertEndpoint(), input);
		
		JsonUtils.prettyPrint(output);
		
		//TODO
	}
}
