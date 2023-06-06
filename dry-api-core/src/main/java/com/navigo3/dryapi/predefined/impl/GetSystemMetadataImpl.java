package com.navigo3.dryapi.predefined.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.exec.json.JsonBatchRequest;
import com.navigo3.dryapi.core.exec.json.JsonBatchResponse;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.validation.Validator;
import com.navigo3.dryapi.predefined.def.GetSystemMetadataEndpoint;
import com.navigo3.dryapi.predefined.def.GetSystemMetadataEndpoint.SystemTypesDescription;
import com.navigo3.dryapi.predefined.def.ImmutableSystemTypesDescription;
import com.navigo3.dryapi.predefined.params.EmptyResponseParam;

public abstract class GetSystemMetadataImpl<TAppContext extends AppContext, TCallContext extends CallContext, TValidator extends Validator> extends MethodImplementation<EmptyResponseParam, SystemTypesDescription, GetSystemMetadataEndpoint, TAppContext, TCallContext, TValidator> {

	public abstract DryApi<TAppContext, TCallContext, TValidator> getApi();

	@Override
	public void validate(EmptyResponseParam input, TValidator validator) {
	}

	@Override
	public SystemTypesDescription execute(EmptyResponseParam input) {
		return ImmutableSystemTypesDescription.builder()
			.requestTypeSchema(TypeSchema.build(new TypeReference<JsonBatchRequest>() {
			}))
			.responseTypeSchema(TypeSchema.build(new TypeReference<JsonBatchResponse>() {
			}))
			.build();
	}

}
