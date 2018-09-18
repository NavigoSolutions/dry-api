package com.navigo3.dryapi.sample.impls.philosophy;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerResult;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;

public class SolveEverythingImpl extends MethodImplementation<TopAddressInput, IntegerResult, SolveEverythingEndpoint, TestAppContext, TestCallContext, TestValidator> {
	
	@Override
	public void fillClassSecurity(MethodSecurityBuilder<TestAppContext, TestCallContext> security) {
		security.authorization(new True<>());
	}
	
	@Override
	public TestCallContext prepareCallContext(TopAddressInput input) {
		return new TestCallContext();
	}

	@Override
	public void validate(TopAddressInput input, TestValidator validator) {
	}

	@Override
	public IntegerResult execute(TopAddressInput input) {
		return ImmutableIntegerResult.builder().res(42).build();
	}
}
