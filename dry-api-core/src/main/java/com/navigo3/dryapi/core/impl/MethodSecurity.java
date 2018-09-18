package com.navigo3.dryapi.core.impl;

import java.util.Optional;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.security.field.ObjectFieldsSecurityBuilder;
import com.navigo3.dryapi.core.security.field.TypeFieldsSecurity;
import com.navigo3.dryapi.core.util.Consumer3;
import com.navigo3.dryapi.core.util.Validate;

@Value.Immutable
public interface MethodSecurity<TAppContext extends AppContext, TCallContext extends CallContext> {
	SecurityCheck<TAppContext, TCallContext> getAuthorization();
	
	Optional<TypeFieldsSecurity<TAppContext, TCallContext>> getInputFieldsTypeSecurity();
	
	Optional<TypeFieldsSecurity<TAppContext, TCallContext>> getOutputFieldsTypeSecurity();
	
	Optional<Consumer3<TAppContext, TCallContext, ObjectFieldsSecurityBuilder<TAppContext, TCallContext>>> getInputFieldsObjectSecurity();
	
	Optional<Consumer3<TAppContext, TCallContext, ObjectFieldsSecurityBuilder<TAppContext, TCallContext>>> getOutputFieldsObjectSecurity();
	
	@Value.Check default void check() {
		Validate.isFalse(getInputFieldsTypeSecurity().isPresent() && getInputFieldsObjectSecurity().isPresent(), 
			"Input fields security on both type and object is not supported!");
		
		Validate.isFalse(getOutputFieldsTypeSecurity().isPresent() && getOutputFieldsObjectSecurity().isPresent(), 
			"Output fields security on both type and object is not supported!");
	}
}
