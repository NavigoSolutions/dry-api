package com.navigo3.dryapi.sample.impls.math.integer;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodMetadataBuilder;
import com.navigo3.dryapi.core.impl.MethodSecurityBuilder;
import com.navigo3.dryapi.core.path.StructurePath;
import com.navigo3.dryapi.core.security.logic.True;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerOperands;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerResult;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;
import com.navigo3.dryapi.sample.impls.TestValidator;

public class AddIntegersImpl extends MethodImplementation<IntegerOperands, IntegerResult, AddIntegersEndpoint, TestAppContext, TestCallContext, TestValidator> {

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
		validator.checkPresent(StructurePath.key("a"), input.getA());
		validator.checkPresent(StructurePath.key("b"), input.getB());
	}
	
	@Override
	public IntegerResult execute(IntegerOperands input) {
		return ImmutableIntegerResult
			.builder()
			.res(input.getA().orElse(0)+input.getB().orElse(0))
			.build();
	}
}
