package com.navigo3.dryapi.core.security.field;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.security.core.SecurityCheck;

@Value.Immutable
public interface CacheEntry<TAppContext extends AppContext, TCallContext extends CallContext> {
	SecurityCheck<TAppContext, TCallContext> getSecurityCheck();
	boolean getPassed();
}