package com.navigo3.dryapi.test.integration;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.navigo3.dryapi.client.ImmutableRequestsBatchData;
import com.navigo3.dryapi.client.ModifiableRequestData;
import com.navigo3.dryapi.core.exec.json.ImmutableInputOutputMapping;
import com.navigo3.dryapi.core.exec.json.JsonRequest.RequestType;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerOperands;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerOperands;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.NegateIntegersEndpoint;
import com.navigo3.dryapi.test.helpers.RemoteCallsEnvironment;

public class InputOutputMappingTest {
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
		//calculate 1+1
		ModifiableRequestData<IntegerOperands, IntegerResult> first = ModifiableRequestData
			.<IntegerOperands, IntegerResult>create()
			.setUuid(UUID.randomUUID().toString())
			.setInput(ImmutableIntegerOperands.builder().a(1).b(1).build())
			.setMethod(new AddIntegersEndpoint())
			.setRequestType(RequestType.EXECUTE);
		
		//calculate 40 + ?
		ModifiableRequestData<IntegerOperands, IntegerResult> second = ModifiableRequestData
			.<IntegerOperands, IntegerResult>create()
			.setInput(ImmutableIntegerOperands.builder().a(40).build())
			.setMethod(new AddIntegersEndpoint())
			.setRequestType(RequestType.EXECUTE);
		
		//set result of first to ? in second
		second.addInputOutputMappings(ImmutableInputOutputMapping
			.builder()
			.fromUuid(first.getUuid())
			.fromPath(StructurePath.key("res"))
			.toPath(StructurePath.key("b"))
			.build()
		);
		
		env.getApi().callBlockingRaw(ImmutableRequestsBatchData.builder().addRequests(first, second).id(42).build());

		assertEquals(42, second.getOutput().get().getRes().get().intValue());
	}
	
	@Test
	public void test2() {
		//calculate 1+1
		ModifiableRequestData<IntegerOperands, IntegerResult> first = ModifiableRequestData
			.<IntegerOperands, IntegerResult>create()
			.setUuid(UUID.randomUUID().toString())
			.setInput(ImmutableIntegerOperands.builder().a(1).b(1).build())
			.setMethod(new AddIntegersEndpoint())
			.setRequestType(RequestType.EXECUTE);
		
		//calculate - ?
		ModifiableRequestData<IntegerResult, IntegerResult> second = ModifiableRequestData
			.<IntegerResult, IntegerResult>create()
			.setInput(ImmutableIntegerResult.builder().build())
			.setMethod(new NegateIntegersEndpoint())
			.setRequestType(RequestType.EXECUTE);
		
		//set result of first to ? in second
		second.addInputOutputMappings(ImmutableInputOutputMapping
			.builder()
			.fromUuid(first.getUuid())
			.fromPath(StructurePath.empty())
			.toPath(StructurePath.empty())
			.build()
		);
		
		env.getApi().callBlockingRaw(ImmutableRequestsBatchData.builder().addRequests(first, second).id(42).build());

		assertEquals(-2, second.getOutput().get().getRes().get().intValue());
	}
}
