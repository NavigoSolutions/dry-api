package com.navigo3.dryapi.sample.defs.generics;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.navigo3.dryapi.core.def.IOTypeReference;
import com.navigo3.dryapi.core.def.MethodInterface;
import com.navigo3.dryapi.sample.defs.generics.GetGenericDataEndpoint.GenericData;
import com.navigo3.dryapi.sample.defs.math.integer.AddIntegersEndpoint.IntegerOperands;

public class GetGenericDataEndpoint implements MethodInterface<IntegerOperands, GenericData> {

	@Value.Immutable
	@JsonDeserialize(as = ImmutableSomeGenericData.class)
	public interface SomeGenericData {
		String getName();
	}

	public interface GenericDataBase<T> {
		T getData();
	}

	public interface GenericDataBase2<T> extends GenericDataBase<T> {
	}

	@Value.Immutable
	@JsonDeserialize(as = ImmutableGenericData.class)
	public interface GenericData extends GenericDataBase2<SomeGenericData> {
		int getTest();
	}

	@Override
	public String getQualifiedName() {
		return "generic/get";
	}

	@Override
	public String getDescription() {
		return "Test of direct generic types inheritence";
	}

	@Override
	public IOTypeReference<IntegerOperands> getInputType() {
		return new IOTypeReference<IntegerOperands>() {
		};
	}

	@Override
	public IOTypeReference<GenericData> getOutputType() {
		return new IOTypeReference<GenericData>() {
		};
	}

}
