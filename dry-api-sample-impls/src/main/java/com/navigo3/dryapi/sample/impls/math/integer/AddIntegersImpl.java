package com.navigo3.dryapi.sample.impls.math.integer;

import java.util.Optional;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerOperands;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerResult;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestAppValidator;
import com.navigo3.dryapi.sample.impls.TestCallContext;

public class AddIntegersImpl extends MethodImplementation<IntegerOperands, IntegerResult, AddIntegersEndpoint, TestAppContext, TestCallContext> {

	@Override
	public void fillClassSecurity(MethodSecurityBuilder<TestAppContext, TestCallContext> security) {
		security.authorization(new True<>());
	}
	
	@Override
	public TestCallContext prepareCallContext(IntegerOperands input) {
		return new TestCallContext();
	}

	@Override
	public Optional<ValidationData> validate(IntegerOperands input) {
		return TestAppValidator.build(builder->{
			builder.checkPresent(StructurePath.key("a"), input.getA());
			builder.checkPresent(StructurePath.key("b"), input.getB());
		});
	}
	
	@Override
	public IntegerResult execute(IntegerOperands input) {
		return ImmutableIntegerResult
			.builder()
			.res(input.getA().orElse(0)+input.getB().orElse(0))
			.build();
	}
}
