package com.navigo3.dryapi.sample.impls.math.integer;

import java.util.Optional;

import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.path.StructurePathBuilder;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.security.logic.Everyone;
import com.navigo3.dryapi.core.validation.ValidationResult;
import com.navigo3.dryapi.core.validation.ValidationResultBuilder;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerOperands;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;
import com.navigo3.dryapi.sample.defs.math.integer.ImmutableIntegerResult;
import com.navigo3.dryapi.sample.impls.TestAppContext;
import com.navigo3.dryapi.sample.impls.TestCallContext;

public class AddIntegersImpl extends MethodImplementation<IntegerOperands, IntegerResult, TestAppContext, TestCallContext> {

	@Override
	public SecurityCheck<TestAppContext, TestCallContext> getAuthorization() {
		return new Everyone<>();
	}
	
	@Override
	public TestCallContext prepareCallContext(IntegerOperands input) {
		return new TestCallContext();
	}

	@Override
	public Optional<ValidationResult> validate(IntegerOperands input) {
		ValidationResultBuilder builder = ValidationResultBuilder.create();
		
		builder.checkPresent(StructurePathBuilder.create().key("a").build(), input.getA());
		builder.checkPresent(StructurePathBuilder.create().key("b").build(), input.getB());
		
		return Optional.of(builder.build());
	}
	
	@Override
	public IntegerResult execute(IntegerOperands input) {
		return ImmutableIntegerResult
			.builder()
			.res(input.getA().orElse(0)+input.getB().orElse(0))
			.build();
	}
}
