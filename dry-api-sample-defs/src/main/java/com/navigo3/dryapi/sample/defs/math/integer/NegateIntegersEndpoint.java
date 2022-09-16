package com.navigo3.dryapi.sample.defs.math.integer;

import com.navigo3.dryapi.core.def.IOTypeReference;
import com.navigo3.dryapi.core.def.MethodInterface;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;

public class NegateIntegersEndpoint implements MethodInterface<IntegerResult, IntegerResult> {
	@Override
	public String getQualifiedName() {
		return "math/integer/negate";
	}

	@Override
	public String getDescription() {
		return "Negate integer";
	}
	
	@Override
	public IOTypeReference<IntegerResult> getInputType() {
		return new IOTypeReference<IntegerResult>() {};
	}

	@Override
	public IOTypeReference<IntegerResult> getOutputType() {
		return new IOTypeReference<IntegerResult>() {};
	}
}