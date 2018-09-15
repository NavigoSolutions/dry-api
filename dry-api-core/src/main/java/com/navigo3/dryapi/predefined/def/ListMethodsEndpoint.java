package com.navigo3.dryapi.predefined.def;

import java.util.List;

import org.immutables.value.Value;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.predefined.def.ListMethodsEndpoint.MethodBasicDescription;
import com.navigo3.dryapi.predefined.params.VoidParam;

public class ListMethodsEndpoint extends MethodDefinition<VoidParam, List<MethodBasicDescription>> {
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableMethodBasicDescription.class)
	@JsonDeserialize(as = ImmutableMethodBasicDescription.class)
	public interface MethodBasicDescription {
		String getQualifiedName();
		String getDescription();
	}

	@Override
	public String getQualifiedName() {
		return "meta/api-methods-list";
	}

	@Override
	public String getDescription() {
		return "Returns list of API methods";
	}

	@Override
	public TypeReference<VoidParam> getInputType() {
		return new TypeReference<VoidParam>(){};
	}

	@Override
	public TypeReference<List<MethodBasicDescription>> getOutputType() {
		return new TypeReference<List<MethodBasicDescription>>(){};
	}
}
	