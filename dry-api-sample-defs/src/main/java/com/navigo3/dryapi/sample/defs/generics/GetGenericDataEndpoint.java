package com.navigo3.dryapi.sample.defs.generics;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.def.IOTypeReference;
import com.navigo3.dryapi.core.def.MethodInterface;
import com.navigo3.dryapi.sample.defs.generics.GetGenericDataEndpoint.GenericData;

public class GetGenericDataEndpoint implements MethodInterface<Integer, GenericData> {
	
	public interface GenericDataBase<T> {
		T getData();
	}
	
	@Value.Immutable
	public interface GenericData extends GenericDataBase<Integer>{
		
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
	public IOTypeReference<Integer> getInputType() {
		return new IOTypeReference<Integer>() {
		};
	}

	@Override
	public IOTypeReference<GenericData> getOutputType() {
		return new IOTypeReference<GenericData>() {
		};
	}

}
