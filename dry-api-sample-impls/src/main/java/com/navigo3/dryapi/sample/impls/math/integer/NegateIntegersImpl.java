package com.navigo3.dryapi.sample.impls.math.integer;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodMetadataBuilder;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.NegateIntegersEndpoint;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;

public class NegateIntegersImpl extends MethodImplementation<IntegerResult, IntegerResult, NegateIntegersEndpoint, TestAppContext, TestCallContext, TestValidator> {

	@Override
	public void fillClassMetadata(MethodMetadataBuilder<TestAppContext, TestCallContext> metadata) {
	}

	@Override
	public void fillClassSecurity(MethodSecurityBuilder<TestAppContext, TestCallContext> security) {
		security.authorization(new True<>());
	}

	@Override
	public TestCallContext prepareCallContext(IntegerResult input) {
		return new TestCallContext();
	}

	@Override
	public void validate(IntegerResult input, TestValidator validator) {
	}

	@Override
	public IntegerResult execute(IntegerResult input) {
		return ImmutableIntegerResult.builder().res(-input.getRes().orElse(0)).build();
	}
}
