package com.navigo3.dryapi.predefined.def;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.navigo3.dryapi.core.def.IOTypeReference;
import com.navigo3.dryapi.core.def.MethodInterface;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.predefined.def.GetSystemMetadataEndpoint.SystemTypesDescription;
import com.navigo3.dryapi.predefined.params.EmptyResponseParam;

public class GetSystemMetadataEndpoint implements MethodInterface<EmptyResponseParam, SystemTypesDescription> {

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
	public IOTypeReference<EmptyResponseParam> getInputType() {
		return new IOTypeReference<EmptyResponseParam>() {
		};
	}

	@Override
	public IOTypeReference<SystemTypesDescription> getOutputType() {
		return new IOTypeReference<SystemTypesDescription>() {
		};
	}
}