package com.navigo3.dryapi.core.security.field;

import java.util.Map;

import com.navigo3.dryapi.core.context.AppContext;
import com.navigo3.dryapi.core.context.CallContext;
import com.navigo3.dryapi.core.meta.TypeSchema;
import com.navigo3.dryapi.core.path.TypePath;
import com.navigo3.dryapi.core.security.core.SecurityCheck;

public interface FieldsSecurity<TAppContext extends AppContext, TCallContext extends CallContext> {
	Map<TypePath, SecurityCheck<TAppContext, TCallContext>> getSecurityPerField(TAppContext appContext, TCallContext callContext, TypeSchema schema);
	
	boolean canBeCashed();
}
