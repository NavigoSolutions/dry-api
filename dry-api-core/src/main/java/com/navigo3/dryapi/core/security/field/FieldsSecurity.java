package com.navigo3.dryapi.core.security.field;

import java.util.Map;

import org.immutables.value.Value;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;

@Value.Immutable
public interface FieldsSecurity<TAppContext extends AppContext, TCallContext extends CallContext> {
	Map<TypePath, SecurityCheck<TAppContext, TCallContext>> getSecurityPerField();
}
