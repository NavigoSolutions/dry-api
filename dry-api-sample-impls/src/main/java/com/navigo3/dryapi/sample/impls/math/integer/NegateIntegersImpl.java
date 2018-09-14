package com.navigo3.dryapi.sample.impls.math.integer;

import java.util.Optional;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.NegateIntegersEndpoint;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestAppValidator;
import com.navigo3.dryapi.sample.impls.TestCallContext;

public class NegateIntegersImpl extends MethodImplementation<IntegerResult, IntegerResult, NegateIntegersEndpoint, TestAppContext, TestCallContext> {

	@Override
	public void fillClassSecurity(MethodSecurityBuilder<TestAppContext, TestCallContext> security) {
		security.authorization(new True<>());
	}
	
	@Override
	public TestCallContext prepareCallContext(IntegerResult input) {
		return new TestCallContext();
	}

	@Override
	public Optional<ValidationData> validate(IntegerResult input) {
		return TestAppValidator.build(builder->{
		});
	}
	
	@Override
	public IntegerResult execute(IntegerResult input) {
		return ImmutableIntegerResult
			.builder()
			.res(-input.getRes().orElse(0))
			.build();
	}
}
