package com.navigo3.dryapi.sample.impls.philosophy;

import java.util.Optional;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerResult;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint;
import com.navigo3.dryapi.sample.defs.philosophy.SolveEverythingEndpoint.TopAddressInput;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;

public class SolveEverythingImpl extends MethodImplementation<TopAddressInput, IntegerResult, SolveEverythingEndpoint, TestAppContext, TestCallContext> {
	
	@Override
	public void fillClassSecurity(MethodSecurityBuilder<TestAppContext, TestCallContext> security) {
		security.authorization(new True<>());
	}
	
	@Override
	public TestCallContext prepareCallContext(TopAddressInput input) {
		return new TestCallContext();
	}

	@Override
	public Optional<ValidationData> validate(TopAddressInput input) {
		return Optional.empty();
	}

	@Override
	public IntegerResult execute(TopAddressInput input) {
		return ImmutableIntegerResult.builder().res(42).build();
	}
}
