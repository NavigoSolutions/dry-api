package com.navigo3.dryapi.predefined.def;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.def.IOTypeReference;
import com.navigo3.dryapi.core.def.MethodInterface;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.predefined.def.GetMethodMetadataEndpoint.MethodFullDescription;
import com.navigo3.dryapi.predefined.params.QualifiedNameParam;

public class GetMethodMetadataEndpoint implements MethodInterface<QualifiedNameParam, MethodFullDescription> {
	
	@Value.Immutable
	public interface SecurityNode {
		String getDescription();
		Optional<List<SecurityNode>> getChildren();
	}
	
	@Value.Immutable
	@JsonSerialize(as = ImmutableMethodFullDescription.class)
	@JsonDeserialize(as = ImmutableMethodFullDescription.class)
	public interface MethodFullDescription {
		String getQualifiedName();
		String getDescription();
		Set<String> getFlags();
		
		TypeSchema getInputTypeSchema();
		
		TypeSchema getOutputTypeSchema();
		
		Optional<SecurityNode> getAuthorization();
	}

	@Override
	public String getQualifiedName() {
		return "meta/api-method";
	}

	@Override
	public String getDescription() {
		return "Get implemention details of method - input and output types description, security";
	}

	@Override
	public IOTypeReference<QualifiedNameParam> getInputType() {
		return new IOTypeReference<QualifiedNameParam>(){};
	}

	@Override
	public IOTypeReference<MethodFullDescription> getOutputType() {
		return new IOTypeReference<MethodFullDescription>(){};
	}
}