package com.navigo3.dryapi.test;

import java.util.Arrays;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint;
import com.navigo3.dryapi.sample.defs.form.FormUpsertEndpoint.Person;
import com.navigo3.dryapi.sample.defs.form.ImmutablePerson;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;
import com.navigo3.dryapi.test.helpers.RemoteCallsEnvironment;

public class Sandbox {

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
	public void test() {
		Person input = ImmutablePerson.builder()
			.name("James")
			.surname("Hook")
			.age(100)
			.secretNumber(42)
			.putColorsToFavoriteNumbers("red", Arrays.asList(1, 5, 7))
			.putColorsToFavoriteNumbers("blue", Arrays.asList(42))
			.putColorsToFavoriteNumbers("green", Arrays.asList(2, 10, 15, 78))
			.build();

		/* IdResult output = */env.getApi().executeBlocking(new FormUpsertEndpoint(), input);
//		JsonUtils.prettyPrint(output);
	}

	@Test
	@Ignore
	public void test2() {
		/* IntegerResult output2 = */env.getApi()
			.executeBlocking(new SolveEverythingEndpoint(), TopAddressInput.createSampleData());

//		JsonUtils.prettyPrint(output2);
	}
}
