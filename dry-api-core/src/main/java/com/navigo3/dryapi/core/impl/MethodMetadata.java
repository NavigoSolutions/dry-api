package com.navigo3.dryapi.core.impl;

import java.util.Set;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;

@Value.Immutable
public interface MethodMetadata<TAppContext extends AppContext, TCallContext extends CallContext> {
	Set<String> getFlags();

	@Value.Default
	default boolean getDisableAllowedFields() {
		return false;
	}
}
