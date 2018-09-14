package com.navigo3.dryapi.sample.defs.math.integer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;

public class NegateIntegersEndpoint extends MethodDefinition<IntegerResult, IntegerResult> {

	@Override
	public TypeReference<IntegerResult> getInputType() {
		return new TypeReference<IntegerResult>() {};
	}

	@Override
	public TypeReference<IntegerResult> getOutputType() {
		return new TypeReference<IntegerResult>() {};
	}

	@Override
	public String getQualifiedName() {
		return "math/integer/negate";
	}

	@Override
	public String getDescription() {
		return "Negate integer";
	}
}