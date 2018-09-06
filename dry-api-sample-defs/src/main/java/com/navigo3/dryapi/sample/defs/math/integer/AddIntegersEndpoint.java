package com.navigo3.dryapi.sample.defs.math.integer;

import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerOperands;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerResult;

public class AddIntegersEndpoint extends MethodDefinition<IntegerOperands, IntegerResult> {
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableIntegerOperands.class)
	@JsonDeserialize(as = ImmutableIntegerOperands.class)
	public interface IntegerOperands {
		Optional<Integer> getA();
		Optional<Integer> getB();
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableIntegerResult.class)
	@JsonDeserialize(as = ImmutableIntegerResult.class)
	public interface IntegerResult {
		int getRes();
	}

	@Override
	public TypeReference<IntegerOperands> getInputType() {
		return new TypeReference<IntegerOperands>() {};
	}

	@Override
	public TypeReference<IntegerResult> getOutputType() {
		return new TypeReference<IntegerResult>() {};
	}

	@Override
	public String getQualifiedName() {
		return "math/integer/add";
	}

	@Override
	public String getDescription() {
		return "Adds two integers and returns result";
	}
}