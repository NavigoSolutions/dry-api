package com.navigo3.dryapi.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.def.DryApi;
import com.navigo3.dryapi.core.def.MethodDefinition;
import com.navigo3.dryapi.core.impl.MethodImplementation;
import com.navigo3.dryapi.core.util.Validate;
import com.navigo3.dryapi.core.validation.ValidationData;
import com.navigo3.dryapi.predefined.def.ImmutableMethodBasicDescription;
import com.navigo3.dryapi.predefined.def.ListMethodsEndpoint;
import com.navigo3.dryapi.predefined.def.ListMethodsEndpoint.MethodBasicDescription;
import com.navigo3.dryapi.predefined.params.VoidParam;

public abstract class ListMethodsImpl<TAppContext extends AppContext, TCallContext extends CallContext> 
	extends MethodImplementation<VoidParam, List<MethodBasicDescription>, ListMethodsEndpoint, TAppContext, TCallContext> {
	
	public abstract DryApi<TAppContext, TCallContext> getApi();
	
	@Override
	public Optional<ValidationData> validate(VoidParam input) {
		return Optional.empty();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<MethodBasicDescription> execute(VoidParam input) {
		List<MethodBasicDescription> res = new ArrayList<>();
		
		getApi().getAllQualifiedNames().forEach(qualifiedName->{
			Optional<MethodDefinition> def = getApi().lookupDefinition(qualifiedName);
			
			Validate.isPresent(def);
			
			res.add(ImmutableMethodBasicDescription
				.builder()
				.qualifiedName(def.get().getQualifiedName())
				.description(def.get().getDescription())
				.build()
			);
		});
		
		return res;
	}
}
