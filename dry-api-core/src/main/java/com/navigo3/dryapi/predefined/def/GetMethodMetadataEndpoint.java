package com.navigo3.dryapi.predefined.def;

import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.predefined.def.GetMethodMetadataEndpoint.MethodFullDescription;
import com.navigo3.dryapi.predefined.params.QualifiedNameParam;

public class GetMethodMetadataEndpoint extends MethodDefinition<QualifiedNameParam, MethodFullDescription> {
	
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
		
		SecurityNode getAuthorization();
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
	public TypeReference<QualifiedNameParam> getInputType() {
		return new TypeReference<QualifiedNameParam>(){};
	}

	@Override
	public TypeReference<MethodFullDescription> getOutputType() {
		return new TypeReference<MethodFullDescription>(){};
	}
}