package com.navigo3.dryapi.core.impl;

import java.util.Optional;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.security.core.SecurityCheck;
import com.navigo3.dryapi.core.security.field.FieldsSecurity;

@Value.Immutable
public interface MethodSecurity<TAppContext extends AppContext, TCallContext extends CallContext> {
	SecurityCheck<TAppContext, TCallContext> getAuthorization();
	
	Optional<FieldsSecurity<TAppContext, TCallContext>> getInputFieldsSecurity();
	
	Optional<FieldsSecurity<TAppContext, TCallContext>> getOutputFieldsSecurity();
}
