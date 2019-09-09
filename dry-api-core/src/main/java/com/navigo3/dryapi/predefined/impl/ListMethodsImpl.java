package com.navigo3.dryapi.predefined.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.impl.MethodMetadata;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.Validator;
import com.navigo3.dryapi.predefined.def.ImmutableMethodBasicDescription;
import com.navigo3.dryapi.predefined.def.ListMethodsEndpoint;
import com.navigo3.dryapi.predefined.def.ListMethodsEndpoint.MethodBasicDescription;
import com.navigo3.dryapi.predefined.params.VoidParam;

public abstract class ListMethodsImpl<TAppContext extends AppContext, TCallContext extends CallContext, TValidator extends Validator> 
	extends MethodImplementation<VoidParam, List<MethodBasicDescription>, ListMethodsEndpoint, TAppContext, TCallContext, TValidator> {
	
	public abstract DryApi<TAppContext, TCallContext, TValidator> getApi();
	
	@Override
	public void validate(VoidParam input, TValidator validator) {
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<MethodBasicDescription> execute(VoidParam input) {
		List<MethodBasicDescription> res = new ArrayList<>();
		
		getApi().getAllQualifiedNames().forEach(qualifiedName->{
			Optional<MethodDefinition> def = getApi().lookupDefinition(qualifiedName);
			Optional<MethodMetadata<TAppContext, TCallContext>> meta = getApi().lookupFlags(qualifiedName);
			
			Validate.isPresent(def);
			
			res.add(ImmutableMethodBasicDescription
				.builder()
				.qualifiedName(def.get().getQualifiedName())
				.description(def.get().getDescription())
				.flags(meta.get().getFlags())
				.build()
			);
		});
		
		return res;
	}
}
