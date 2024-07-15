package com.navigo3.dryapi.test;

import static org.junit.Assert.assertEquals;

import java.util.concurrent.CompletableFuture;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.sample.defs.generics.GetGenericDataEndpoint;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerOperands;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerOperands;
import com.navigo3.dryapi.test.helpers.RemoteCallsEnvironment;

public class RemoteCalls {
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
	public void testAsync() {
		IntegerOperands input = ImmutableIntegerOperands.builder().a(40).b(2).build();

		CompletableFuture<IntegerResult> future = env.getApi()
			.executeAsync(new AddIntegersEndpoint(), input, output -> {
				// here would be async code
				assertEquals(42, output.getRes());
			});

		assertEquals(42, future.join().getRes().get().intValue());
	}

	@Test
	public void testBlocking() {
		IntegerOperands input = ImmutableIntegerOperands.builder().a(40).b(2).build();

		IntegerResult output = env.getApi().executeBlocking(new AddIntegersEndpoint(), input);

		assertEquals(42, output.getRes().get().intValue());
	}

	@Test
	public void testAsyncValidation() {
		IntegerOperands input = ImmutableIntegerOperands.builder().a(40).build();

		CompletableFuture<ValidationData> future = env.getApi()
			.validateAsync(new AddIntegersEndpoint(), input, validation -> {
				// here would be async code
				assertEquals(1, validation.getItems().size());
			});

		assertEquals(1, future.join().getItems().size());
	}

	@Test
	public void testBlockingValidation() {
		IntegerOperands input = ImmutableIntegerOperands.builder().a(40).build();

		ValidationData output = env.getApi().validateBlocking(new AddIntegersEndpoint(), input);

		assertEquals(1, output.getItems().size());
	}

	@Test
	public void testGenerics() {
		env.getApi().executeBlocking(new GetGenericDataEndpoint(), 1);
	}
}
