package com.navigo3.dryapi.predefined.impl;

import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.exec.json.JsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchResponse;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.predefined.def.GetSystemMetadataEndpoint;
import com.navigo3.dryapi.predefined.def.GetSystemMetadataEndpoint.SystemTypesDescription;
import com.navigo3.dryapi.predefined.def.ImmutableSystemTypesDescription;
import com.navigo3.dryapi.predefined.params.VoidParam;

public abstract class GetSystemMetadataImpl<TAppContext extends AppContext, TCallContext extends CallContext> 
	extends MethodImplementation<VoidParam, SystemTypesDescription, GetSystemMetadataEndpoint, TAppContext, TCallContext> {

	public abstract DryApi<TAppContext, TCallContext> getApi();
	
	@Override
	public Optional<ValidationData> validate(VoidParam input) {
		return Optional.empty();
	}

	@Override
	public SystemTypesDescription execute(VoidParam input) {
		return ImmutableSystemTypesDescription
			.builder()
			.requestTypeSchema(TypeSchema.build(new TypeReference<JsonBatchRequest>(){}))
			.responseTypeSchema(TypeSchema.build(new TypeReference<JsonBatchResponse>(){}))
			.build();
	}

}