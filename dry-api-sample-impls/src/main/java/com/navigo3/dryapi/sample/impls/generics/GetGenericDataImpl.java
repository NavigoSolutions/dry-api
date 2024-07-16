package com.navigo3.dryapi.sample.impls.generics;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodMetadataBuilder;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.sample.defs.generics.GetGenericDataEndpoint;
import com.navigo3.dryapi.sample.defs.generics.GetGenericDataEndpoint.GenericData;
import com.navigo3.dryapi.sample.defs.generics.ImmutableGenericData;
import com.navigo3.dryapi.sample.defs.generics.ImmutableSomeGenericData;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerOperands;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;

public class GetGenericDataImpl extends MethodImplementation<IntegerOperands, GenericData, GetGenericDataEndpoint, TestAppContext, TestCallContext, TestValidator> {

	@Override
	public void fillClassMetadata(MethodMetadataBuilder<TestAppContext, TestCallContext> metadata) {
	}

	@Override
	public void fillClassSecurity(MethodSecurityBuilder<TestAppContext, TestCallContext> security) {
		security.authorization(new True<>());
	}

	@Override
	public TestCallContext prepareCallContext(IntegerOperands input) {
		return new TestCallContext();
	}

	@Override
	public void validate(IntegerOperands input, TestValidator validator) {
	}

	@Override
	public GenericData execute(IntegerOperands input) {
		return ImmutableGenericData.builder().test(42)
			.data(ImmutableSomeGenericData.builder().name("Hello, world").build())
			.build();
	}

}
