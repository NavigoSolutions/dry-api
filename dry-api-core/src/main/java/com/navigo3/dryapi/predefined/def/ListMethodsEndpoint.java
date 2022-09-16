package com.navigo3.dryapi.predefined.def;

import java.util.List;
import java.util.Set;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.def.IOTypeReference;
import com.navigo3.dryapi.core.def.MethodInterface;
import com.navigo3.dryapi.predefined.def.ListMethodsEndpoint.MethodBasicDescription;
import com.navigo3.dryapi.predefined.params.VoidParam;

public class ListMethodsEndpoint implements MethodInterface<VoidParam, List<MethodBasicDescription>> {
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableMethodBasicDescription.class)
	@JsonDeserialize(as = ImmutableMethodBasicDescription.class)
	public interface MethodBasicDescription {
		String getQualifiedName();
		String getDescription();
		Set<String> getFlags();
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
	public IOTypeReference<VoidParam> getInputType() {
		return new IOTypeReference<VoidParam>(){};
	}

	@Override
	public IOTypeReference<List<MethodBasicDescription>> getOutputType() {
		return new IOTypeReference<List<MethodBasicDescription>>(){};
	}
}
	