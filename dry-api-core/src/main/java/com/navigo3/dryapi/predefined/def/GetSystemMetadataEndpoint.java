package com.navigo3.dryapi.predefined.def;

import org.immutables.value.Value;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.predefined.def.GetSystemMetadataEndpoint.SystemTypesDescription;
import com.navigo3.dryapi.predefined.params.VoidParam;

public class GetSystemMetadataEndpoint extends MethodDefinition<VoidParam, SystemTypesDescription> {

	@Value.Immutable
	@JsonSerialize(as = ImmutableSystemTypesDescription.class)
	@JsonDeserialize(as = ImmutableSystemTypesDescription.class)
	public interface SystemTypesDescription {
		TypeSchema getRequestTypeSchema();
		TypeSchema getResponseTypeSchema();
	}

	@Override
	public String getQualifiedName() {
		return "meta/api-system-types";
	}

	@Override
	public String getDescription() {
		return "Get description of data types used for dry-api requests";
	}

	@Override
	public TypeReference<VoidParam> getInputType() {
		return new TypeReference<VoidParam>(){};
	}

	@Override
	public TypeReference<SystemTypesDescription> getOutputType() {
		return new TypeReference<SystemTypesDescription>(){};
	}
}